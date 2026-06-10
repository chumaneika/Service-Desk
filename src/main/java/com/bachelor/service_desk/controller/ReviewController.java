package com.bachelor.service_desk.controller;

import com.bachelor.service_desk.dto.ReviewCreateDTO;
import com.bachelor.service_desk.dto.ReviewResponseDTO;
import com.bachelor.service_desk.entity.ReviewEntity;
import com.bachelor.service_desk.entity.ReviewOwner;
import com.bachelor.service_desk.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{reviewOwner}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponseDTO> createReview(@RequestBody @Valid ReviewCreateDTO dto, @PathVariable ReviewOwner reviewOwner) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(dto, reviewOwner));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<ReviewResponseDTO>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<ReviewEntity>> getReviewsByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(reviewService.getReviewsByOwner(ownerId));
    }

    @GetMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ReviewEntity> findById(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.findById(reviewId));
    }
}
