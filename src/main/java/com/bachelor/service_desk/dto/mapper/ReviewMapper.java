package com.bachelor.service_desk.dto.mapper;

import com.bachelor.service_desk.dto.ReviewCreateDTO;
import com.bachelor.service_desk.dto.ReviewResponseDTO;
import com.bachelor.service_desk.entity.ReviewEntity;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewEntity toEntity(ReviewCreateDTO dto) {
        return new ReviewEntity(dto.title(), dto.description(), null, null);
    }

    public ReviewResponseDTO toDto(ReviewEntity entity) {
        Long ownerId = entity.getOwner() == null ? null : entity.getOwner().getId();
        Long requestId = entity.getRequest() == null ? null : entity.getRequest().getId();

        return new ReviewResponseDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                ownerId,
                requestId
        );
    }
}
