package com.vk.itmo.segmentation.controller;

import com.vk.itmo.segmentation.dto.UserResponse;
import com.vk.itmo.segmentation.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Segment Management", description = "API для управления пользователями")
public class UserController {
    private final UserService userService;

    @GetMapping("/get-all")
    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех пользователей.")
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userService.getAllUsers(pageable);
    }
}
