package com.hungpham.mappers;

import com.hungpham.dtos.UserDto;
import com.hungpham.entity.UserEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(
        componentModel = "spring",
        uses = {UuidBinaryMapper.class}
)
public interface UserMapper {

    // =========================
    // Entity -> DTO
    // =========================
    @Mapping(source = "id", target = "id", qualifiedByName = "toUuid")
    @Mapping(source = "createdDate", target = "createdDate", qualifiedByName = "timeToString")
    @Mapping(source = "updatedDate", target = "updatedDate", qualifiedByName = "timeToString")
    UserDto toDto(UserEntity entity);

    // =========================
    // Custom helpers
    // =========================
    @Named("timeToString")
    default String timeToString(java.time.LocalDateTime t) {
        return t != null ? t.toString() : null;
    }
}
