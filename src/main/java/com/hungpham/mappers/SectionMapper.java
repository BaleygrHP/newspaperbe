package com.hungpham.mappers;

import com.hungpham.dtos.SectionDto;
import com.hungpham.entity.SectionEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UuidBinaryMapper.class, DateTimeMapper.class})
public interface SectionMapper {

    @Mappings({
            @Mapping(target = "id", source = "id", qualifiedByName = "toUuid"),
            @Mapping(target = "createdDate", source = "createdDate", qualifiedByName = "ldtToString"),
            @Mapping(target = "updatedDate", source = "updatedDate", qualifiedByName = "ldtToString")
    })
    SectionDto toDto(SectionEntity entity);

    @Mappings({
            @Mapping(target = "id", source = "id", qualifiedByName = "toBytes"),
            @Mapping(target = "createdDate", source = "createdDate", qualifiedByName = "stringToLdt"),
            @Mapping(target = "updatedDate", source = "updatedDate", qualifiedByName = "stringToLdt")
    })
    SectionEntity toEntity(SectionDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdDate", ignore = true),
            @Mapping(target = "updatedDate", ignore = true)
    })
    void updateEntityFromDto(SectionDto dto, @MappingTarget SectionEntity entity);
}
