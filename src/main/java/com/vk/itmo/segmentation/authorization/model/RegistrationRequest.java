package com.vk.itmo.segmentation.authorization.model;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RegistrationRequest {
    @Nonnull
    @Email(message = "Email must be a valid email address")
    private String email;

    @Nonnull
    @Size(max = 255, message = "Full name must be at most 255 characters")
    private String userName;

    @Nonnull
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    private String password;
}
