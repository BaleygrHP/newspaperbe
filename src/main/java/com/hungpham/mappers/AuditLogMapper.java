package com.hungpham.mappers;

import com.hungpham.dtos.AuditLogDto;
import com.hungpham.entity.AuditLogEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UuidBinaryMapper.class, DateTimeMapper.class})
public interface AuditLogMapper {

    @Mappings({
            @Mapping(target = "id", source = "id"), // Long id
            @Mapping(target = "actorId", source = "id", qualifiedByName = "toUuid"),
            @Mapping(target = "entityId", source = "entityId", qualifiedByName = "toUuid"),
            @Mapping(target = "createdDate", source = "createdAt", qualifiedByName = "ldtToString")
    })
    AuditLogDto toDto(AuditLogEntity entity);

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "actor", ignore = true), // set ở service
            @Mapping(target = "entityId", source = "entityId", qualifiedByName = "toUuid"),
            @Mapping(target = "createdAt", source = "createdDate", qualifiedByName = "stringToLdt")
    })
    AuditLogEntity toEntity(AuditLogDto dto);
}
