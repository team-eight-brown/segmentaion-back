package com.vk.itmo.segmentation.service;

import com.vk.itmo.segmentation.dto.*;
import com.vk.itmo.segmentation.entity.Segment;
import com.vk.itmo.segmentation.entity.User;
import com.vk.itmo.segmentation.exception.ForbiddenException;
import com.vk.itmo.segmentation.exception.NotFoundException;
import com.vk.itmo.segmentation.repository.SegmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SegmentService {

    private final SegmentRepository segmentRepository;
    private final UserService userService;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final TransactionTemplate masterTransactionTemplate;

    public Segment findById(Long segmentId) {
        return segmentRepository.findById(segmentId)
                .orElseThrow(() -> new NotFoundException("Not found segment with id: " + segmentId));
    }

    public Optional<Segment> findByName(String segmentName) {
        return segmentRepository.findByName(segmentName);
    }

    // Создание сегмента
    public SegmentResponse createSegment(SegmentCreateRequest request) {
        if (!userService.isCurrentUserAdmin()) {
            throw new ForbiddenException("Текущий пользователь не является администратором");
        }
        if (segmentRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Сегмент с именем '" + request.name() + "' уже существует.");
        }
        var segment = new Segment()
                .setName(request.name())
                .setDescription(request.description());
        segment = segmentRepository.save(segment);
        return mapToSegmentResponse(segment);
    }

    // Удаление сегмента
    public void deleteSegment(Long segmentId) {
        if (!userService.isCurrentUserAdmin()) {
            throw new ForbiddenException("Текущий пользователь не является администратором");
        }
        var segment = findById(segmentId);
        segmentRepository.delete(segment);
    }

    // Редактирование сегмента
    public SegmentResponse updateSegment(Long segmentId, SegmentUpdateRequest updateRequest) {
        if (!userService.isCurrentUserAdmin()) {
            throw new ForbiddenException("Текущий пользователь не является администратором");
        }
        var segment = findById(segmentId);
        if (!segment.getName().equals(updateRequest.name()) && segmentRepository.existsByName(updateRequest.name())) {
            throw new IllegalArgumentException("Сегмент с именем '" + updateRequest.name() + "' уже существует.");
        }
        if (updateRequest.name() != null) {
            segment.setName(updateRequest.name());
        }
        if (updateRequest.description() != null) {
            segment.setDescription(updateRequest.description());
        }
        segment = segmentRepository.save(segment);
        return mapToSegmentResponse(segment);
    }

    // Добавление пользователя в сегмент
    public void addUserToSegment(UsersToSegmentRequest request, Long segmentId) {
        if (!userService.isCurrentUserAdmin()) {
            throw new ForbiddenException("Текущий пользователь не является администратором");
        }
        masterTransactionTemplate.execute(_ -> {
            var user = userService.findById(request.userId());
            var segment = findById(segmentId);
            if (user.getSegments().contains(segment)) {
                throw new IllegalStateException("Пользователь уже находится в сегменте.");
            }
            log.info("Adding user {} to segment {}", user.getId(), segment.getId());
            user.getSegments().add(segment);
            userService.save(user);
            return null;
        });
    }

    // Удаление пользователя из сегмента
    @Transactional
    public void removeUserFromSegment(UsersToSegmentRequest request, Long segmentId) {
        if (!userService.isCurrentUserAdmin()) {
            throw new ForbiddenException("Текущий пользователь не является администратором");
        }
        var user = userService.findById(request.userId());
        var segment = findById(segmentId);
        if (!user.getSegments().contains(segment)) {
            throw new IllegalStateException("Пользователь не состоит в сегменте.");
        }
        user.getSegments().remove(segment);
        segment.getUsers().remove(user);
        segmentRepository.save(segment);
    }

    // Получение сегментов пользователя
    public Page<SegmentResponse> getUserSegments(Long userId, Pageable pageable) {
        userService.findById(userId);
        return segmentRepository.findByUsersId(userId, pageable)
                .map(SegmentService::mapToSegmentResponse);
    }

    public Page<SegmentResponse> getAllSegments(String name, String description, Long id, Pageable pageable) {
        Specification<Segment> spec = Specification.where(SegmentSpecification.hasName(name))
                .and(SegmentSpecification.hasDescription(description))
                .and(SegmentSpecification.hasId(id));
        return segmentRepository.findAll(spec, pageable).map(SegmentService::mapToSegmentResponse);
    }

    private static SegmentResponse mapToSegmentResponse(Segment segment) {
        return new SegmentResponse(
                segment.getId().toString(),
                segment.getName(),
                segment.getDescription()
        );
    }

    public void randomDistributeUsersIntoSegments(DistributionRequest distributionRequest) {
        if (!userService.isCurrentUserAdmin()) {
            throw new ForbiddenException("Текущий пользователь не является администратором");
        }
        log.info("Distribution started: {}", distributionRequest);
        Segment segment = segmentRepository.findByName(distributionRequest.segmentName())
                .orElseThrow(() -> new RuntimeException("Сегмент не найден"));

        List<User> users = userService.findAll();

        int totalUsers = users.size();
        int usersToAssign = (int) (totalUsers * (distributionRequest.percentage() / 100));


        List<User> randomUsers = getRandomUsers(users, usersToAssign);
        List<Callable<Void>> tasks = new ArrayList<>(usersToAssign);
        randomUsers.forEach(user -> tasks.add(() -> {
            addUserToSegment(new UsersToSegmentRequest(user.getId()), segment.getId());
            return null;
        }));

        log.info("Total tasks: {}", tasks.size());
        try {
            // Используем invokeAll() для запуска всех задач
            List<Future<Void>> results = executor.invokeAll(tasks);

            // Ждем завершения всех задач и обрабатываем результаты (если нужно)
            for (Future<Void> result : results) {
                try {
                    result.get(); // Это блокирует до завершения каждой задачи
                } catch (ExecutionException e) {
                    log.error("Ошибка при выполнении задачи", e);
                }
            }
            log.info("User distribution completed.");
        } catch (InterruptedException e) {
            log.error("Execution interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    private static List<User> getRandomUsers(List<User> users, int count) {
        Collections.shuffle(users);
        return users.subList(0, count);
    }
}
