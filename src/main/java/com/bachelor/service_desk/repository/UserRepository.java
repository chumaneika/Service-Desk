package com.bachelor.service_desk.repository;

import com.bachelor.service_desk.entity.Role;
import com.bachelor.service_desk.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByRole(Role role);
    boolean existsByNumberPhone(String numberPhone);
    Optional<UserEntity> findByNumberPhone(String numberPhone);
    List<UserEntity> findAllByRole(Role role);

    @Query("""
       SELECT u
       FROM UserEntity u
       WHERE LOWER(CONCAT(u.name, ' ', u.surname)) LIKE LOWER(CONCAT('%', :search, '%'))
          OR LOWER(CONCAT(u.surname, ' ', u.name)) LIKE LOWER(CONCAT('%', :search, '%'))
       """)
    List<UserEntity> searchByFullName(@Param("search") String search);
}
