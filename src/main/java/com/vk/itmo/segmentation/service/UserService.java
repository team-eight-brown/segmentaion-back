package com.vk.itmo.segmentation.service;

import com.vk.itmo.segmentation.dto.UserResponse;
import com.vk.itmo.segmentation.entity.AdminUser;
import com.vk.itmo.segmentation.entity.User;
import com.vk.itmo.segmentation.exception.NotFoundException;
import com.vk.itmo.segmentation.repository.AdminUserRepository;
import com.vk.itmo.segmentation.repository.UserRepository;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;

    @Nonnull
    public User findById(@Nonnull Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
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
}
