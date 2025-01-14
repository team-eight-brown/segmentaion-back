package com.vk.itmo.segmentation.service;

import com.vk.itmo.segmentation.entity.Segment;
import org.springframework.data.jpa.domain.Specification;

public class SegmentSpecification {

    public static Specification<Segment> hasName(String name) {
        return (root, query, criteriaBuilder) ->
                name == null ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"
                );
    }

    public static Specification<Segment> hasDescription(String description) {
        return (root, query, criteriaBuilder) ->
                description == null ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), "%" + description.toLowerCase() + "%"
                );
    }

    public static Specification<Segment> hasId(Long id) {
        return (root, query, criteriaBuilder) ->
                id == null ? null : criteriaBuilder.equal(root.get("id"), id);
    }
}