package com.vk.itmo.segmentation.authorization.controller;

import com.vk.itmo.segmentation.authorization.model.AuthorizationRequest;
import com.vk.itmo.segmentation.authorization.model.JwtResponse;
import com.vk.itmo.segmentation.authorization.model.RegistrationRequest;
import com.vk.itmo.segmentation.authorization.service.AuthenticationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authorization", description = "API для авторизации и регистрации пользователей")
public class AuthorizationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public JwtResponse register(@RequestBody @Valid RegistrationRequest request) {
        return JwtResponse.builder()
                .token(authenticationService.register(request))
                .build();
    }

    @PostMapping("/login")
    public JwtResponse login(@RequestBody @Valid AuthorizationRequest request) {
        return JwtResponse.builder()
                .token(authenticationService.login(request))
                .build();
    }
}
