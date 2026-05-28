package com.bachelor.service_desk.controller;

import com.bachelor.service_desk.dto.RequestCreateDTO;
import com.bachelor.service_desk.entity.RequestEntity;
import com.bachelor.service_desk.entity.RequestStatus;
import com.bachelor.service_desk.service.RequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RequestEntity> createRequest(@RequestBody @Valid RequestCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(requestService.createRequest(dto));
    }

//    @GetMapping("/by-user/{userId}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
//    public ResponseEntity<List<RequestEntity>> getRequestsByUser(@PathVariable Long userId) throws AccessDeniedException {
//        return ResponseEntity.ok(requestService.getRequestsByUser(userId));
//    }

    @GetMapping("/by-responsible/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<RequestEntity>> getRequestsByResponsible(@PathVariable Long userId) throws AccessDeniedException {
        return ResponseEntity.ok(requestService.getRequestsByResponsible(userId));
    }

    @GetMapping("/created")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<RequestEntity>> handleCreatedRequests() {
        return ResponseEntity.ok(requestService.handleCreatedRequests());
    }

    @PatchMapping("/{requestId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RequestEntity> changeStatus(@PathVariable Long requestId, @RequestParam String status) {
        return ResponseEntity.ok(requestService.changeStatus(requestId, status));
    }

    @PatchMapping("/{requestId}/feedback")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> leaveFeedback(@PathVariable Long requestId, @RequestParam String feedback) {
        requestService.leaveFeedback(requestId, feedback);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<RequestEntity> findById(@PathVariable Long requestId) {
        return ResponseEntity.ok(requestService.findById(requestId));
    }

    @GetMapping("/by-status/{userId}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<List<RequestEntity>> findAllRequestsByStatus(
            @PathVariable Long userId,
            @RequestParam(required = false) RequestStatus status
    ) {
        return ResponseEntity.ok(requestService.findAllRequestsByStatus(userId, status));
    }

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<RequestEntity>> findAllRequestsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(requestService.findAllRequestsByUser(userId));
    }
}
