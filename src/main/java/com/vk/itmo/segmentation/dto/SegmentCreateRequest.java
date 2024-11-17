package com.vk.itmo.segmentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SegmentCreateRequest(@NotNull @NotBlank String name, @NotNull @NotBlank String description) {
}
