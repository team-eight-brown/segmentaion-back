package com.vk.itmo.segmentation.authorization.model;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuthorizationRequest {
    @Nonnull
    @Size(max = 255, message = "Password must be between 8 and 255 characters")
    private String username;

    @Nonnull
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    private String password;
}
