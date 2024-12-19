package com.vk.itmo.segmentation.repository;

import com.vk.itmo.segmentation.entity.Filter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilterRepository extends JpaRepository<Filter, Long> {
}
