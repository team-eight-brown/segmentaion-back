package com.vk.itmo.segmentation.controller;

import com.vk.itmo.segmentation.dto.*;
import com.vk.itmo.segmentation.exception.ForbiddenException;
import com.vk.itmo.segmentation.service.SegmentService;
import com.vk.itmo.segmentation.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/segments")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Segment Management", description = "API для управления сегментами и их связями с пользователями")
public class SegmentController {

    private final SegmentService segmentService;
    private final UserService userService;

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
        segmentService.addUserToSegmentWithCheck(dto, segmentId);
    }

    @DeleteMapping("/{segmentId}/users")
    @Operation(summary = "Удалить пользователя из сегмента", description = "Удаляет пользователя из указанного сегмента.")
    public void removeUserFromSegment(@PathVariable Long segmentId, @RequestBody @Valid UsersToSegmentRequest dto) {
        segmentService.removeUserFromSegment(dto, segmentId);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Получить сегменты пользователя", description = "Возвращает список сегментов, в которых состоит пользователь.")
    public Page<SegmentResponse> getUserSegments(@PathVariable Long userId, Pageable pageable) {
        return segmentService.getUserSegments(userId, pageable);
    }

    @GetMapping
    @Operation(summary = "Получить все сегменты", description = "Возвращает список всех сегментов с поддержкой пагинации.")
    public Page<SegmentResponse> getAllSegments(
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "description", required = false) String description,
            Pageable pageable) {
        return segmentService.getAllSegments(name, description, id, pageable);
    }

    @PostMapping("/distribute-random")
    @Operation(summary = "Рандомное распределение пользователей", description = "Рандомное распределение пользователей.")
    public ResponseEntity<DefaultResponse> distributeUsers(@RequestBody @Valid DistributionRequest distributionRequest) {
        if (segmentService.findByName(distributionRequest.segmentName()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new DefaultResponse("Сегмент с таким именем не существует"));
        }
        if (!userService.isCurrentUserAdmin()) {
            throw new ForbiddenException("Текущий пользователь не является администратором");
        }

        segmentService.randomDistributeUsersIntoSegments(distributionRequest);
        return ResponseEntity.ok(new DefaultResponse("Процесс сегментирования пользователей запущен успешно"));
    }
}
