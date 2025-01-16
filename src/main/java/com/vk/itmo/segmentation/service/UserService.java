package com.vk.itmo.segmentation.service;

import com.vk.itmo.segmentation.dto.AnalystResponse;
import com.vk.itmo.segmentation.dto.UserResponse;
import com.vk.itmo.segmentation.entity.AdminUser;
import com.vk.itmo.segmentation.entity.Role;
import com.vk.itmo.segmentation.entity.User;
import com.vk.itmo.segmentation.exception.NotFoundException;
import com.vk.itmo.segmentation.repository.AdminUserRepository;
import com.vk.itmo.segmentation.repository.RoleRepository;
import com.vk.itmo.segmentation.repository.UserRepository;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static java.util.stream.Collectors.toSet;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;
    private final RoleRepository roleRepository;
    private static final String ADMIN_ROLE = "Admin";
    private final TransactionTemplate transactionTemplate;

    @Nonnull
    public User findById(@Nonnull Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id не был найден: " + userId));
    }

    /**
     * Получение пользователя по имени пользователя
     *
     * @return пользователь
     */
    @Nonnull
    public AdminUser getByUsername(@Nonnull String userName) {
        return adminUserRepository.findByUsername(userName)
                .orElseThrow();
    }

    /**
     * Получение пользователя по имени пользователя
     * <p>
     * Нужен для Spring Security
     *
     * @return пользователь
     */
    @Nonnull
    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    /**
     * Сохранение пользователя админа
     *
     * @return сохраненный пользователь
     */
    @Nonnull
    public AdminUser save(@Nonnull AdminUser userEntity) {
        return adminUserRepository.save(userEntity);
    }

    /**
     * Сохранение пользователя
     *
     * @return сохраненный пользователь
     */
    @Nonnull
    public User save(@Nonnull User userEntity) {
        return userRepository.save(userEntity);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findAll(PageRequest pageRequest) {
        return userRepository.findAll(pageRequest).getContent();
    }

    public List<User> saveAll(List<User> users) {
        return userRepository.saveAll(users);
    }

    @Nonnull
    public Page<UserResponse> getAllUsers(@Nullable Integer id,
                                          @Nullable String login,
                                          @Nullable String email,
                                          @Nullable String segmentName,
                                          @Nullable String ipAddress,
                                          @Nonnull Pageable pageable) {
        Specification<User> spec = Specification.where(UserSpecification.hasId(id))
                .and(UserSpecification.hasLogin(login))
                .and(UserSpecification.hasEmail(email))
                .and(UserSpecification.hasSegmentName(segmentName))
                .and(UserSpecification.hasIpAddress(ipAddress));
        return userRepository.findAll(spec, pageable).map(UserService::mapToUserResponse);
    }

    @Nonnull
    private static UserResponse mapToUserResponse(@Nonnull User user) {
        return new UserResponse(
                user.getId(),
                user.getLogin(),
                user.getEmail(),
                user.getIpAddress()
        );
    }

    public long count() {
        return userRepository.count();
    }

    public AnalystResponse getCurrentUser() {
        return transactionTemplate.execute(_ -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            AdminUser currentUser = (AdminUser) authentication.getPrincipal();
            AdminUser fullUser = adminUserRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return new AnalystResponse(
                    fullUser.getId(),
                    fullUser.getUsername(),
                    fullUser.getEmail(),
                    fullUser.getRoles().stream().map(Role::getName).collect(toSet())
            );
        });

    }

    public boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AdminUser currentUser = (AdminUser) authentication.getPrincipal();
        return currentUser.getRoles().stream().anyMatch(role -> role.getName().equals(ADMIN_ROLE));
    }

    public void setRole(long roleId, long userId) {
        var user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        var role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new RuntimeException("Роль не найдена"));
        user.getRoles().add(role);
        adminUserRepository.save(user);
    }

    public void removeRole(long roleId, long userId) {
        var user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        var role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Роль не найдена"));
        user.getRoles().remove(role);
        adminUserRepository.save(user);
    }
}
