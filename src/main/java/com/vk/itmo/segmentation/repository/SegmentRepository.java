package com.vk.itmo.segmentation.repository;

import com.vk.itmo.segmentation.entity.Segment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SegmentRepository extends JpaRepository<Segment, Long> {
    boolean existsByName(String name);
    Page<Segment> findByUsersId(Long userId, Pageable pageable);
    Optional<Segment> findByName(String name);
}