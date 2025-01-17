package com.vk.itmo.segmentation.controller;

import com.vk.itmo.segmentation.dto.DefaultResponse;
import com.vk.itmo.segmentation.dto.FilterDistributeRequest;
import com.vk.itmo.segmentation.service.DistributionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/filter")
@Tag(name = "Segment Management", description = "API для управления фильтрами пользователей при помощи регулярных выражений")
public class FilterController {
    private final DistributionService distributionService;

    @PostMapping("/distribute")
    @Operation(summary = "Распределение пользователей по фильтру", description = "Распределение пользователей по фильтру")
    public ResponseEntity<DefaultResponse> addUserToSegment(@RequestBody @Valid FilterDistributeRequest request) {
        distributionService.distributeByFilter(request);
        return ResponseEntity.ok(new DefaultResponse("Процесс сегментирования пользователей по фильтру запущен успешно"));
    }
}
