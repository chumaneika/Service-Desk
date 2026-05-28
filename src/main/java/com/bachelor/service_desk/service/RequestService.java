package com.bachelor.service_desk.service;

import com.bachelor.service_desk.dto.RequestCreateDTO;
import com.bachelor.service_desk.dto.mapper.RequestMapper;
import com.bachelor.service_desk.entity.*;
import com.bachelor.service_desk.repository.RequestRepository;
import com.bachelor.service_desk.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;


@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;

    @Transactional
    // Пользователь - создание заявки
    public RequestEntity createRequest(RequestCreateDTO dto) {

        UserEntity userCreator = userRepository.findById(dto.createdById())
                .orElseThrow(() -> new RuntimeException("User not found"));

        RequestEntity request = requestMapper.toEntity(dto);
        request.markAsActive();
        request.assignCreator(userCreator);

        return requestRepository.save(request);
    }

    @Transactional
    // Админ и старший админ - Поиск задач определенного заказчика
    public List<RequestEntity> getRequestsByUser(Long id) throws AccessDeniedException {

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));

        if (user.getRole() != Role.USER) {
            throw new AccessDeniedException("Only users can access their requests");
        }

        return user.getRequests();
    }

    @Transactional
    // Админ и старший админ - Поиск задач определенного исполнителя
    public List<RequestEntity> getRequestsByResponsible(Long id) throws AccessDeniedException {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));

        if (user.getRole() == Role.USER) {
            throw new AccessDeniedException("Users cannot access their requests");
        }

        return user.getRequests();
    }

    // Админ и старший админ - Поиск задач со статусом СОЗДАНО
    @Transactional
    public List<RequestEntity> handleCreatedRequests() {
        return requestRepository.findAll().stream()
                .filter(x -> x.getStatus() == RequestStatus.CREATED)
                .toList();
    }

    @Transactional
    // Админ - изменение статуса заявки
    public RequestEntity changeStatus(Long requestId, String status) {

        RequestEntity request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        RequestStatus requestStatus;

        try {
            requestStatus = RequestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid request status");
        }

        request.changeStatus(requestStatus);
        return requestRepository.save(request);
    }

    @Transactional
    // Пользователь - оставить отзыв о выполненной работе
    public void leaveFeedback(Long requestId, String feedback) {
        RequestEntity request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Request is not found"));

        ReviewOwner reviewOwner;

        try {
            reviewOwner = ReviewOwner.valueOf(feedback.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid review status");
        }

        request.changeReviewOwner(reviewOwner);
        requestRepository.save(request);

    }

    @Transactional
    // Админ, старший админ и пользователь со своими заявками
    public RequestEntity findById(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));
    }

    public List<RequestEntity> findAllRequestsByStatus(Long userId, RequestStatus status) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));

        if (status == null) {
            return requestRepository.findAllByCreatedBy(user);
        } else if (status == RequestStatus.IN_PROGRESS) {
            return requestRepository.findAllByStatus(RequestStatus.IN_PROGRESS);
        } else if (status == RequestStatus.COMPLETED) {
            return requestRepository.findAllByStatus(RequestStatus.COMPLETED);
        } else {
            throw new IllegalArgumentException("Invalid request status");
        }

        // todo нужно отрегулировать права запроса на получение заявок определенного статуса
    }

    public List<RequestEntity> findAllRequestsByUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User is not found"));

        return requestRepository.findAllByCreatedBy(user);
    }
}
