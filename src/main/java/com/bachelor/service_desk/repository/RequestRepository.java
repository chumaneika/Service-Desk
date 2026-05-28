package com.bachelor.service_desk.repository;

import com.bachelor.service_desk.entity.RequestEntity;
import com.bachelor.service_desk.entity.RequestStatus;
import com.bachelor.service_desk.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<RequestEntity, Long> {
    List<RequestEntity> findAllByCreatedBy(UserEntity user);
    List<RequestEntity> findAllByStatus(RequestStatus status);
}
