package com.vk.itmo.segmentation.authorization.model;

import jakarta.annotation.Nonnull;
import lombok.Builder;

@Builder
public record JwtResponse(@Nonnull String token) {
}
