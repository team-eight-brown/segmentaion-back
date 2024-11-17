package com.vk.itmo.segmentation.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;


@Entity
@Table(name = "filters")
public class Filter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "segment_id", nullable = false)
    private Segment segment;

    @Column(name = "filter_expression", nullable = false, columnDefinition = "TEXT")
    private String filterExpression;

    @Column(name = "user_percentage", nullable = false)
    private double userPercentage;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private Instant createdAt;
}
