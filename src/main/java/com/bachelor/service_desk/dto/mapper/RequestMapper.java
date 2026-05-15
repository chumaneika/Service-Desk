package com.bachelor.service_desk.dto.mapper;

import com.bachelor.service_desk.dto.RequestCreateDTO;
import com.bachelor.service_desk.entity.RequestEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RequestMapper {
    RequestEntity toEntity(RequestCreateDTO dto);
}
