package com.bachelor.service_desk.dto.mapper;

import com.bachelor.service_desk.dto.ReviewCreateDTO;
import com.bachelor.service_desk.entity.ReviewEntity;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewEntity toEntity(ReviewCreateDTO dto) {
        return new ReviewEntity(dto.title(), dto.description(), null, null);
    }
}
