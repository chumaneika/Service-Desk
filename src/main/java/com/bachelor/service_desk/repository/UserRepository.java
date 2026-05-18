package com.bachelor.service_desk.repository;

import com.bachelor.service_desk.entity.Role;
import com.bachelor.service_desk.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByRole(Role role);
    boolean existsByNumberPhone(String numberPhone);
    Optional<UserEntity> findByNumberPhone(String numberPhone);
}
