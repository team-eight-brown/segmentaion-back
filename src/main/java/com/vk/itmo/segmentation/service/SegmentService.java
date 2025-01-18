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
import org.springframework.scheduling.annotation.Async;
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
    private final TransactionTemplate masterTransactionTemplate;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

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

    public void addUserToSegmentWithCheck(UsersToSegmentRequest request, Long segmentId){
        if (!userService.isCurrentUserAdmin()) {
            throw new ForbiddenException("Текущий пользователь не является администратором");
        }
        addUserToSegment(request, segmentId);
    }

    // Добавление пользователя в сегмент
    public void addUserToSegment(UsersToSegmentRequest request, Long segmentId) {
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

    public Segment findSegmentByName(String name){
        return findByName(name)
                .orElseThrow(() -> new RuntimeException("Сегмент не найден"));
    }

    @Async
    public void randomDistributeUsersIntoSegments(DistributionRequest distributionRequest) {
        Segment segment = findSegmentByName(distributionRequest.segmentName());

        log.info("Distribution started, percentage: {}, segmentName: {}", distributionRequest.percentage(), segment.getName());
        long totalUsers = userService.count();
        int usersToAssign = (int) (totalUsers * (distributionRequest.percentage() / 100));

        List<User> randomUsers = userService.getRandomLimitUsers(usersToAssign);

        long timeS = System.currentTimeMillis();

        List<Callable<Void>> tasks = new ArrayList<>();

        randomUsers.forEach(user -> tasks.add(() -> {
            addUserToSegment(new UsersToSegmentRequest(user.getId()), segment.getId());
            return null;
        }));

        try {
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        long timeE = System.currentTimeMillis() - timeS;
        log.info("User distribution completed in {}", timeE);
    }
}
