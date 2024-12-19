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

import java.util.List;

import static com.vk.itmo.segmentation.entity.enums.FilterType.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributionService {
    private final UserRepository userRepository;
    private final SegmentService segmentService;
    private final SegmentRepository segmentRepository;
    private final FilterRepository filterRepository;

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
        users.forEach(
                user -> segmentService
                        .addUserToSegment(new UsersToSegmentRequest(user.getId()), savedFilter.getSegment().getId())
        );
    }

    private FilterType getFilterType(FilterDistributeType type) {
        return switch (type) {
            case IP_REGEXP -> IP_REGEXP;
            case EMAIL_REGEXP -> EMAIL_REGEXP;
            case LOGIN_REGEXP -> LOGIN_REGEXP;
        };
    }
}
