package com.bachelor.service_desk.service;

import com.bachelor.service_desk.dto.ReviewCreateDTO;
import com.bachelor.service_desk.dto.mapper.ReviewMapper;
import com.bachelor.service_desk.entity.RequestEntity;
import com.bachelor.service_desk.entity.ReviewEntity;
import com.bachelor.service_desk.entity.ReviewOwner;
import com.bachelor.service_desk.entity.UserEntity;
import com.bachelor.service_desk.repository.RequestRepository;
import com.bachelor.service_desk.repository.ReviewRepository;
import com.bachelor.service_desk.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final ReviewMapper reviewMapper;

    @Transactional
    // Пользователь - оставить отзыв
    public ReviewEntity createReview(ReviewCreateDTO dto, ReviewOwner reviewOwner) {
        if (reviewRepository.existsByRequestId(dto.request())) {
            throw new IllegalArgumentException("Review for this request already exists");
        }

        UserEntity user = userRepository.findById(dto.owner())
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));

        RequestEntity request = requestRepository.findById(dto.request())
                .orElseThrow(() -> new EntityNotFoundException("Request is not found"));

        request.changeReviewOwner(reviewOwner);
        ReviewEntity review = reviewMapper.toEntity(dto);
        review.assignCreator(user);
        review.assignRequest(request);

        return reviewRepository.save(review);
    }

    @Transactional
    public ReviewEntity findById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review is not found"));
    }

    @Transactional
    // Админ и старший админ - просмотр всех отзывов
    public List<ReviewEntity> getAllReviews() {
        return reviewRepository.findAll();
    }

    @Transactional
    // Админ и старший админ - просмотр всех отзывов определенного исполнителя
    public List<ReviewEntity> getReviewsByOwner(Long ownerId) {

        UserEntity owner = userRepository.findById(ownerId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found"));

        return owner.getReviews();
    }
}
