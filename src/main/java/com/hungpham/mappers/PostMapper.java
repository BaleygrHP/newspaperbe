package com.hungpham.mappers;
import com.hungpham.dtos.PostDto;
import com.hungpham.entity.PostEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UuidBinaryMapper.class, DateTimeMapper.class, SectionEnumMapper.class})
public interface PostMapper {
    // Entity -> DTO
    @Mappings({
            @Mapping(target = "id", source = "id", qualifiedByName = "toUuid"),
            @Mapping(target = "publishedAt", source = "publishedAt", qualifiedByName = "ldtToString"),
            // Entity.section (enum) -> Dto.section (string)
            @Mapping(target = "section", source = "section", qualifiedByName = "fromSectionEnum")
    })
    PostDto toDto(PostEntity entity);

    // DTO -> Entity
    @Mappings({
            @Mapping(target = "id", source = "id", qualifiedByName = "toBytes"),
            @Mapping(target = "publishedAt", source = "publishedAt", qualifiedByName = "stringToLdt"),
            // Dto.section (string) -> Entity.section (enum)
            @Mapping(target = "section", source = "section", qualifiedByName = "toSectionEnum")
    })
    PostEntity toEntity(PostDto dto);

    // Update existing entity from dto (ignore null)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "publishedAt", source = "publishedAt", qualifiedByName = "stringToLdt"),
            @Mapping(target = "section", source = "section", qualifiedByName = "toSectionEnum")
    })
    void updateEntityFromDto(PostDto dto, @MappingTarget PostEntity entity);
}
