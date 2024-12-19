package com.vk.itmo.segmentation.controller;

import com.vk.itmo.segmentation.dto.FilterDistributeRequest;
import com.vk.itmo.segmentation.dto.UsersToSegmentRequest;
import com.vk.itmo.segmentation.entity.Filter;
import com.vk.itmo.segmentation.service.DistributionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/filter")
@Tag(name = "Segment Management", description = "API для управления фильтрами пользователей при помощи регулярных выражений")
public class FilterController {
    private final DistributionService distributionService;

    @PostMapping("/distribute")
    @Operation(summary = "", description = "Распределение пользователей по фильтру")
    public void addUserToSegment(@RequestBody @Valid FilterDistributeRequest request) {
        distributionService.distributeByFilter(request);
    }
}
