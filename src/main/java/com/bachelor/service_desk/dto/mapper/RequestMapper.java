package com.bachelor.service_desk.dto.mapper;

import com.bachelor.service_desk.dto.RequestCreateDTO;
import com.bachelor.service_desk.entity.RequestEntity;
import org.springframework.stereotype.Component;

@Component
public class RequestMapper {

    public RequestEntity toEntity(RequestCreateDTO dto) {
        return new RequestEntity(dto.title(), dto.description());
    }
}
