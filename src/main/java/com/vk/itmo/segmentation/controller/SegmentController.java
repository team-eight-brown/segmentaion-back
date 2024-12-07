package com.vk.itmo.segmentation.controller;

import com.vk.itmo.segmentation.dto.SegmentCreateRequest;
import com.vk.itmo.segmentation.dto.SegmentResponse;
import com.vk.itmo.segmentation.dto.SegmentUpdateRequest;
import com.vk.itmo.segmentation.dto.UsersToSegmentRequest;
import com.vk.itmo.segmentation.service.SegmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/segments")
@Tag(name = "Segment Management", description = "API для управления сегментами и их связями с пользователями")
public class SegmentController {

    private final SegmentService segmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать новый сегмент", description = "Создает новый сегмент с заданными параметрами.")
    public ResponseEntity<SegmentResponse> createSegment(@RequestBody @Valid SegmentCreateRequest request) {
        return ResponseEntity.status(201).body(segmentService.createSegment(request));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить сегмент", description = "Удаляет существующий сегмент по его идентификатору.")
    public void deleteSegment(@PathVariable Long id) {
        segmentService.deleteSegment(id);
    }


    @PutMapping("/{id}")
    @Operation(summary = "Обновить сегмент", description = "Обновляет данные сегмента")
    public ResponseEntity<SegmentResponse> updateSegment(@PathVariable Long id,
                                                         @RequestBody @Valid SegmentUpdateRequest request) {
        return ResponseEntity.ok(segmentService.updateSegment(id, request));
    }

    @PostMapping("/{segmentId}/users")
    @Operation(summary = "Добавить пользователя в сегмент", description = "Добавляет пользователя в указанный сегмент.")
    public void addUserToSegment(@PathVariable Long segmentId, @RequestBody @Valid UsersToSegmentRequest dto) {
        segmentService.addUserToSegment(dto, segmentId);
    }

    @DeleteMapping("/{segmentId}/users")
    @Operation(summary = "Удалить пользователя из сегмента", description = "Удаляет пользователя из указанного сегмента.")
    public void removeUserFromSegment(@PathVariable Long segmentId, @RequestBody @Valid UsersToSegmentRequest dto) {
        segmentService.removeUserFromSegment(dto, segmentId);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Получить сегменты пользователя", description = "Возвращает список сегментов, в которых состоит пользователь.")
    public List<SegmentResponse> getUserSegments(@PathVariable Long userId) {
        return segmentService.getUserSegments(userId);
    }
}
