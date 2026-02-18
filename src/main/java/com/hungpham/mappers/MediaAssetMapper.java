package com.hungpham.mappers;

import com.hungpham.dtos.MediaAssetDto;
import com.hungpham.entity.MediaAssetEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UuidBinaryMapper.class, DateTimeMapper.class})
public interface MediaAssetMapper {
    @Mappings({
            @Mapping(target = "id", source = "id", qualifiedByName = "toUuid"),
            @Mapping(target = "ownerId", source = "owner.id", qualifiedByName = "toUuid"),
            @Mapping(target = "createdDate", source = "createdDate", qualifiedByName = "ldtToString"),
            @Mapping(target = "takenAt", source = "takenAt", dateFormat = "yyyy-MM-dd")
    })
    MediaAssetDto toDto(MediaAssetEntity entity);

    @Mappings({
            @Mapping(target = "id", source = "id", qualifiedByName = "toBytes"),
            @Mapping(target = "owner", ignore = true),
            @Mapping(target = "createdDate", source = "createdDate", qualifiedByName = "stringToLdt"),
            @Mapping(target = "takenAt", source = "takenAt", dateFormat = "yyyy-MM-dd")
    })
    MediaAssetEntity toEntity(MediaAssetDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "owner", ignore = true),
            @Mapping(target = "createdDate", ignore = true),
            @Mapping(target = "takenAt", source = "takenAt", dateFormat = "yyyy-MM-dd")
    })
    void updateEntityFromDto(MediaAssetDto dto, @MappingTarget MediaAssetEntity entity);
}
