package com.vk.itmo.segmentation.authorization.service;

import com.vk.itmo.segmentation.authorization.model.AuthorizationRequest;
import com.vk.itmo.segmentation.authorization.model.RegistrationRequest;
import com.vk.itmo.segmentation.entity.AdminUser;
import com.vk.itmo.segmentation.repository.RoleRepository;
import com.vk.itmo.segmentation.service.UserService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private static final String DEFAULT_ROLE = "Analyst";
    private final RoleRepository roleRepository;

    /**
     * Регистрация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    @Nonnull
    public String register(@Nonnull RegistrationRequest request) {
        var initialRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + DEFAULT_ROLE));
        var user = AdminUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(initialRole))
                .createdAt(Instant.now())
                .build();

        userService.save(user);
        return jwtService.generateToken(user);
    }

    /**
     * Аутентификация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    @Nonnull
    public String login(@Nonnull AuthorizationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));
        var user = userService
                .userDetailsService()
                .loadUserByUsername(request.getUsername());
        return jwtService.generateToken(user);
    }
}
