package com.vk.itmo.segmentation.service;

import com.vk.itmo.segmentation.dto.DistributionRequest;
import com.vk.itmo.segmentation.dto.SegmentCreateRequest;
import com.vk.itmo.segmentation.dto.SegmentResponse;
import com.vk.itmo.segmentation.dto.SegmentUpdateRequest;
import com.vk.itmo.segmentation.dto.UsersToSegmentRequest;
import com.vk.itmo.segmentation.entity.Segment;
import com.vk.itmo.segmentation.entity.User;
import com.vk.itmo.segmentation.exception.NotFoundException;
import com.vk.itmo.segmentation.repository.SegmentRepository;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class SegmentService {

    private final SegmentRepository segmentRepository;
    private final UserService userService;
    private static final int USER_BATCH_SIZE = 100;
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public Segment findById(Long segmentId) {
        return segmentRepository.findById(segmentId)
                .orElseThrow(() -> new NotFoundException("Not found segment with id: " + segmentId));
    }

    // Создание сегмента
    public SegmentResponse createSegment(SegmentCreateRequest request) {
        if (segmentRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Segment with name '" + request.name() + "' already exists.");
        }
        var segment = new Segment()
                .setName(request.name())
                .setDescription(request.description());
        segment = segmentRepository.save(segment);
        return mapToSegmentResponse(segment);
    }

    // Удаление сегмента
    public void deleteSegment(Long segmentId) {
        var segment = findById(segmentId);
        segmentRepository.delete(segment);
    }

    // Редактирование сегмента
    public SegmentResponse updateSegment(Long segmentId, SegmentUpdateRequest updateRequest) {
        var segment = findById(segmentId);
        if (!segment.getName().equals(updateRequest.name()) && segmentRepository.existsByName(updateRequest.name())) {
            throw new IllegalArgumentException("Segment with name '" + updateRequest.name() + "' already exists.");
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
        var user = userService.findById(request.userId());
        var segment = findById(segmentId);
        if (user.getSegments().contains(segment)) {
            throw new IllegalStateException("User is already in the segment.");
        }
        user.getSegments().add(segment);
        userService.save(user);
    }

    // Удаление пользователя из сегмента
    @Transactional
    public void removeUserFromSegment(UsersToSegmentRequest request, Long segmentId) {
        var user = userService.findById(request.userId());
        var segment = findById(segmentId);
        if (!user.getSegments().contains(segment)) {
            throw new IllegalStateException("User is not part of the segment.");
        }
        user.getSegments().remove(segment);
        segment.getUsers().remove(user);
        segmentRepository.save(segment);
    }

    // Получение сегментов пользователя
    public Page<SegmentResponse> getUserSegments(Long userId, Pageable pageable) {
        userService.findById(userId);
        return segmentRepository.findByUsersId(userId, pageable)
                .map(this::mapToSegmentResponse);
    }

    public Page<SegmentResponse> getAllSegments(Pageable pageable) {
        return segmentRepository.findAll(pageable).map(this::mapToSegmentResponse);
    }

    private SegmentResponse mapToSegmentResponse(Segment segment) {
        return new SegmentResponse(
                segment.getId().toString(),
                segment.getName(),
                segment.getDescription()
        );
    }

    @Transactional
    public void randomDistributeUsersIntoSegments(DistributionRequest distributionRequest) {
        double percentage = distributionRequest.percentage();

        long totalUsers = userService.count();
        if (totalUsers == 0) {
            return;
        }

        int usersToDistribute = (int) (totalUsers * (percentage / 100));
        if (usersToDistribute <= 0) {
            return;
        }

        AtomicInteger toDistribute = new AtomicInteger(usersToDistribute);

        var segment = getSegmentByName(distributionRequest.segment());

        int totalPages = (int) Math.ceil((double) totalUsers / USER_BATCH_SIZE);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int pageNumber = 0; pageNumber < totalPages; pageNumber++) {
            int currentPage = pageNumber;
           tasks.add(() -> {
               distributeOnePage(currentPage, toDistribute, segment);
               return null;
           });
        }
        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Распределяем пользователей из одной страницы.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void distributeOnePage(int pageNumber,
                                  AtomicInteger toDistribute,
                                  Segment segment) {
        if (toDistribute.get() <= 0) {
            return;
        }

        List<User> users = userService.findAll(PageRequest.of(pageNumber, USER_BATCH_SIZE));
        if (users.isEmpty()) {
            return;
        }

        Collections.shuffle(users);

        int currentLimit = toDistribute.get();
        if (currentLimit <= 0) {
            return;
        }

        int toAssign = Math.min(currentLimit, users.size());

        List<User> usersForDistribution = users.subList(0, toAssign);

        for (int i = 0; i < usersForDistribution.size(); i++) {
            User user = usersForDistribution.get(i);
            if (!user.getSegments().contains(segment)) {
                user.getSegments().add(segment);
                segment.getUsers().add(user);
            }
        }
        userService.saveAll(usersForDistribution);

        toDistribute.addAndGet(-toAssign);
    }

    @NonNull
    private Segment getSegmentByName(@NonNull String name) {
        return segmentRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Distributed segment are not exists"));
    }
}
