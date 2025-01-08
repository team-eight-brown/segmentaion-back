package com.vk.itmo.segmentation.service;

import com.vk.itmo.segmentation.dto.FilterDistributeRequest;
import com.vk.itmo.segmentation.dto.FilterDistributeType;
import com.vk.itmo.segmentation.dto.UsersToSegmentRequest;
import com.vk.itmo.segmentation.entity.Filter;
import com.vk.itmo.segmentation.entity.User;
import com.vk.itmo.segmentation.entity.enums.FilterType;
import com.vk.itmo.segmentation.repository.FilterRepository;
import com.vk.itmo.segmentation.repository.SegmentRepository;
import com.vk.itmo.segmentation.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.vk.itmo.segmentation.entity.enums.FilterType.EMAIL_REGEXP;
import static com.vk.itmo.segmentation.entity.enums.FilterType.IP_REGEXP;
import static com.vk.itmo.segmentation.entity.enums.FilterType.LOGIN_REGEXP;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributionService {
    private final UserRepository userRepository;
    private final SegmentService segmentService;
    private final SegmentRepository segmentRepository;
    private final FilterRepository filterRepository;
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public void distributeByFilter(@NonNull FilterDistributeRequest request) {
        var segment = segmentRepository.findById(request.segmentId()).orElseThrow();
        var filter = Filter.builder()
                .filterType(getFilterType(request.type()))
                .segment(segment)
                .filterExpression(request.regexp())
                .build();
        var savedFilter = filterRepository.save(filter);

        log.info("Pattern: {}", request.regexp());
        List<User> users = switch (request.type()) {
            case LOGIN_REGEXP -> userRepository.findByLoginPattern(request.regexp());
            case EMAIL_REGEXP -> userRepository.findByEmailPattern(request.regexp());
            case IP_REGEXP -> userRepository.findByIpPattern(request.regexp());
        };
        log.info("Distribute segments: {}", users);

        List<Callable<Void>> tasks = new ArrayList<>();

        users.forEach(user -> tasks.add(
                () -> {
                    segmentService.addUserToSegment(new UsersToSegmentRequest(user.getId()), savedFilter.getSegment().getId());
                    return null;
                })
        );
        try {
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private FilterType getFilterType(FilterDistributeType type) {
        return switch (type) {
            case IP_REGEXP -> IP_REGEXP;
            case EMAIL_REGEXP -> EMAIL_REGEXP;
            case LOGIN_REGEXP -> LOGIN_REGEXP;
        };
    }
}
