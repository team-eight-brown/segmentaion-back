package com.vk.itmo.segmentation.service;

import com.vk.itmo.segmentation.dto.SegmentCreateRequest;
import com.vk.itmo.segmentation.dto.SegmentResponse;
import com.vk.itmo.segmentation.dto.SegmentUpdateRequest;
import com.vk.itmo.segmentation.dto.UsersToSegmentRequest;
import com.vk.itmo.segmentation.entity.Segment;
import com.vk.itmo.segmentation.exception.NotFoundException;
import com.vk.itmo.segmentation.repository.SegmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.Long;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SegmentService {

    private final SegmentRepository segmentRepository;
    private final UserService userService;

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
    public void addUserToSegment(UsersToSegmentRequest request) {
        var user = userService.findById(request.userId());
        var segment = findById(request.segmentId());
        if (user.getSegments().contains(segment)) {
            throw new IllegalStateException("User is already in the segment.");
        }
        user.getSegments().add(segment);
        segment.getUsers().add(user);
        segmentRepository.save(segment);
    }

    // Удаление пользователя из сегмента
    @Transactional
    public void removeUserFromSegment(UsersToSegmentRequest request) {
        var user = userService.findById(request.userId());
        var segment = findById(request.segmentId());
        if (!user.getSegments().contains(segment)) {
            throw new IllegalStateException("User is not part of the segment.");
        }
        user.getSegments().remove(segment);
        segment.getUsers().remove(user);
        segmentRepository.save(segment);
    }

    // Получение сегментов пользователя
    public List<SegmentResponse> getUserSegments(Long userId) {
        var user = userService.findById(userId);
        var userSegments = user.getSegments();
        return userSegments.stream()
                .map(segment -> mapToSegmentResponse(segment))
                .toList();
    }

    private SegmentResponse mapToSegmentResponse(Segment segment) {
        return new SegmentResponse(
                segment.getId().toString(),
                segment.getName(),
                segment.getDescription()
        );
    }
}
