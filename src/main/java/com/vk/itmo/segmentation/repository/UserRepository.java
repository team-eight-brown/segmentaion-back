package com.vk.itmo.segmentation.repository;

import com.vk.itmo.segmentation.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT * FROM users WHERE email ~ :pattern", nativeQuery = true)
    List<User> findByEmailPattern(@Param("pattern") String pattern);

    @Query(value = "SELECT * FROM users WHERE login ~ :pattern", nativeQuery = true)
    List<User> findByLoginPattern(@Param("pattern") String pattern);

    @Query(value = "SELECT * FROM users WHERE ip_address ~ :pattern", nativeQuery = true)
    List<User> findByIpPattern(@Param("pattern") String pattern);
}
