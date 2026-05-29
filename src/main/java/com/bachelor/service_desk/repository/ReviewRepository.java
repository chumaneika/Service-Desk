package com.bachelor.service_desk.repository;

import com.bachelor.service_desk.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    boolean existsByRequestId(Long requestId);
}
