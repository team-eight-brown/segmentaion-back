package com.vk.itmo.segmentation.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.NonNull;

public record FilterDistributeRequest(
        @NonNull FilterDistributeType type,
        @NonNull @NotEmpty String regexp,
        @NonNull Long segmentId) {
}
