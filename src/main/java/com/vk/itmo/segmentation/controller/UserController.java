package com.vk.itmo.segmentation.controller;

import com.vk.itmo.segmentation.dto.AnalystResponse;
import com.vk.itmo.segmentation.dto.DefaultResponse;
import com.vk.itmo.segmentation.dto.ChangeRoleRequest;
import com.vk.itmo.segmentation.dto.UserResponse;
import com.vk.itmo.segmentation.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
            @RequestParam(name = "segment_name", required = false) String segmentName,
            @RequestParam(name = "ip_address", required = false) String ipAddress,
            Pageable pageable) {
        return userService.getAllUsers(id, login, email, segmentName, ipAddress, pageable);
    }

    @GetMapping("/analyst/get-current")
    @Operation(summary = "Получить информацию об авторизованном аналитике", description = "Возвращает информацию об авторизованном аналитике.")
    public ResponseEntity<AnalystResponse> getAllUsers() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PostMapping("/analyst/set-role")
    @Operation(summary = "Добавить роль аналитику", description = "Добавляет роль аналитику")
    public ResponseEntity<DefaultResponse> setRole(@RequestBody ChangeRoleRequest request) {
        if (!userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new DefaultResponse("Текущий пользователь не является администратором"));
        }
        userService.setRole(request.roleId(), request.userId());
        return ResponseEntity.ok(new DefaultResponse("Роль успешно установлена"));
    }

    @PostMapping("/analyst/remove-role")
    @Operation(summary = "Удалить роль у аналитика", description = "Удаление роли у аналитика")
    public ResponseEntity<DefaultResponse> deleteRole(@RequestBody ChangeRoleRequest request) {
        if (!userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new DefaultResponse("Текущий пользователь не является администратором"));
        }
        userService.removeRole(request.roleId(), request.userId());
        return ResponseEntity.ok(new DefaultResponse("Роль успешно удалена"));
    }
}
