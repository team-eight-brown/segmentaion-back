package com.vk.itmo.segmentation.dto;

import java.util.Set;

public record AnalystResponse(
        long id,
        String login,
        String email,
        Set<String> roles
) {
}