package com.bachelor.service_desk.dto.mapper;

import com.bachelor.service_desk.dto.RequestCreateDTO;
import com.bachelor.service_desk.dto.ReviewCreateDTO;
import com.bachelor.service_desk.entity.RequestEntity;
import com.bachelor.service_desk.entity.ReviewEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    ReviewEntity toEntity(ReviewCreateDTO dto);
}
