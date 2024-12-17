package com.vk.itmo.segmentation.service;

import com.vk.itmo.segmentation.dto.*;
import com.vk.itmo.segmentation.entity.Segment;
import com.vk.itmo.segmentation.entity.User;
import com.vk.itmo.segmentation.exception.NotFoundException;
import com.vk.itmo.segmentation.repository.SegmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SegmentService {

    private final SegmentRepository segmentRepository;
    private final UserService userService;
    private static int USER_BATCH_SIZE = 100;

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
        segment.getUsers().add(user);
        segmentRepository.save(segment);
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
        List<Segment> segments = segmentRepository.findAll();
        if (segments.isEmpty()) {
            return;
        }
        double fraction = percentage / 100;
        long totalUsers = userService.count();
        if (totalUsers == 0) {
            return;
        }
        int usersToDistribute = (int) (totalUsers * fraction);
        int userPageNumber = 0;
        List<User> users;
        do {
            users = userService.findAll(PageRequest.of(userPageNumber, USER_BATCH_SIZE));
            if (users.isEmpty()) {
                break;
            }
            List<User> modifiableUsers = new ArrayList<>(users);
            Collections.shuffle(modifiableUsers);
            List<User> usersForDistribution = users.subList(0, Math.min(users.size(), usersToDistribute));
            for (int i = 0; i < usersForDistribution.size(); i++) {
                User user = usersForDistribution.get(i);
                Segment segment = segments.get(i % segments.size());
                if (!user.getSegments().contains(segment)) {
                    user.getSegments().add(segment);
                    segment.getUsers().add(user);
                }
            }
            userService.saveAll(usersForDistribution);
            usersToDistribute -= usersForDistribution.size();
            userPageNumber++;
        } while (usersToDistribute > 0);
    }
}
