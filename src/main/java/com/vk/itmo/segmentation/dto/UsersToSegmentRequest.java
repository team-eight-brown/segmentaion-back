package com.vk.itmo.segmentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UsersToSegmentRequest(@NotNull @Min(1) Long userId, @NotNull @Min(1) Long segmentId){
};
