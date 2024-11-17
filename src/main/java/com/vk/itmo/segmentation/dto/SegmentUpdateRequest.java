package com.vk.itmo.segmentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SegmentUpdateRequest(@NotNull Long id, @NotBlank String name, @NotBlank String description) {
}
