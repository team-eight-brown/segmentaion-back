package com.vk.itmo.segmentation.service;

import com.vk.itmo.segmentation.entity.User;
import com.vk.itmo.segmentation.exception.NotFoundException;
import com.vk.itmo.segmentation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
    }
}
