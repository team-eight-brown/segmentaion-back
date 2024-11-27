package com.vk.itmo.segmentation.entity;
import com.vk.itmo.segmentation.entity.enums.FilterType;
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

    @Column(name = "user_percentage")
    private Double userPercentage;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "filter_type", nullable = false)
    private FilterType filterType;

}
