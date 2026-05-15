package com.bachelor.service_desk.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "request_seq")
    @SequenceGenerator(
            name = "request_seq",
            sequenceName = "request_seq",
            allocationSize = 10
    )
    private Long id;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private UserEntity createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private UserEntity assignedTo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_owner")
    private ReviewOwner reviewOwner;

    public void changeReviewOwner(ReviewOwner reviewOwner) {
        this.reviewOwner = reviewOwner;
    }

    public void changeStatus(RequestStatus status) {
        this.status = status;
    }

    public void markAsActive() {
        this.status = RequestStatus.CREATED;
    }

    public void assignCreator(UserEntity user) {
        this.createdBy = user;
    }

    @PrePersist
    public void markCreatedNow() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void markUpdatedNow() {
        this.updatedAt = LocalDateTime.now();
    }
}
