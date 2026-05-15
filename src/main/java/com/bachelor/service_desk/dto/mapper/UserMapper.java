package com.bachelor.service_desk.dto.mapper;

import com.bachelor.service_desk.dto.UserCreateDTO;
import com.bachelor.service_desk.dto.UserUpdateFullNameDTO;
import com.bachelor.service_desk.entity.UserEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserEntity toEntity(UserCreateDTO dto);
    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    void updateFullName(
            UserUpdateFullNameDTO dto,
            @MappingTarget UserEntity user
    );
}
