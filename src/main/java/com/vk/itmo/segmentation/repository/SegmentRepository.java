package com.vk.itmo.segmentation.repository;

import com.vk.itmo.segmentation.entity.Segment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SegmentRepository extends JpaRepository<Segment, Long> {
    boolean existsByName(String name);
}