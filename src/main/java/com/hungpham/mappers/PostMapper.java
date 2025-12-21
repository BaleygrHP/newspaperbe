package com.hungpham.mappers;

import com.hungpham.dtos.PostDto;
import com.hungpham.entity.PostEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UuidBinaryMapper.class, DateTimeMapper.class})
public interface PostMapper {
    // Entity -> DTO
    @Mappings({
            @Mapping(target = "id", source = "id", qualifiedByName = "toUuid"),
            // Nếu PostEntity.publishedAt là LocalDateTime và PostDto.publishedAt là String:
            @Mapping(target = "publishedAt", source = "publishedAt", qualifiedByName = "ldtToString"),
    })
    PostDto toDto(PostEntity entity);

    // DTO -> Entity
    @Mappings({
            @Mapping(target = "id", source = "id", qualifiedByName = "toBytes"),
            @Mapping(target = "publishedAt", source = "publishedAt", qualifiedByName = "stringToLdt")
    })
    PostEntity toEntity(PostDto dto);

    // Update existing entity from dto (ignore null)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "publishedAt", source = "publishedAt", qualifiedByName = "stringToLdt")
    })
    void updateEntityFromDto(PostDto dto, @MappingTarget PostEntity entity);
}
