package com.vk.itmo.segmentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.NonNull;

public record DistributionRequest(@Min(0) @Max(100) double percentage,
                                  @NonNull @NotEmpty String segmentName) {
}
