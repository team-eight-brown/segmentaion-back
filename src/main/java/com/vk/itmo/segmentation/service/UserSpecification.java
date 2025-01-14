package com.vk.itmo.segmentation.service;

import com.vk.itmo.segmentation.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    public static Specification<User> hasEmail(String email) {
        return (root, query, criteriaBuilder) ->
                email == null ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%"
                );
    }

    public static Specification<User> hasLogin(String login) {
        return (root, query, criteriaBuilder) ->
                login == null ? null : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("login")), "%" + login.toLowerCase() + "%"
                );
    }

    public static Specification<User> hasId(Integer id) {
        return (root, query, criteriaBuilder) ->
                id == null ? null : criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("id")), id
                );
    }
}
