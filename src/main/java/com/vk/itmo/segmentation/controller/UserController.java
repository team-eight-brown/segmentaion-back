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
import org.springframework.web.bind.annotation.RequestParam;
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
    public Page<UserResponse> getAllUsers(
            @RequestParam(name = "id", required = false) Integer id,
            @RequestParam(name = "login", required = false) String login,
            @RequestParam(name = "email", required = false) String email,
            Pageable pageable) {
        return userService.getAllUsers(id, login, email, pageable);
    }

    @GetMapping("/get-all-by-segment")
    @Operation(summary = "Получить всех пользователей сегмента", description = "Возвращает список всех пользователей сегмента.")
    public Page<UserResponse> getAllUsersOfSegment(
            @RequestParam(name = "segment-name", required = true) String segmentName,
            Pageable pageable) {
        return userService.getAllUsersOfSegment(segmentName, pageable);
    }
}
