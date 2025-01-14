package com.vk.itmo.segmentation.dto;

public record UserResponse(
        long id,
        String login,
        String email,
        String ipAddress
) {
}