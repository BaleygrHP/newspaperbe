package com.hungpham.mappers;

import com.hungpham.dtos.PublicMediaDto;
import com.hungpham.entity.MediaAssetEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = {UuidBinaryMapper.class, DateTimeMapper.class})
public interface PublicMediaMapper {

    @Mappings({
            @Mapping(target = "id", source = "id", qualifiedByName = "toUuid"),
            @Mapping(target = "createdDate", source = "createdDate", qualifiedByName = "ldtToString"),
            @Mapping(target = "updatedDate", source = "updatedDate", qualifiedByName = "ldtToString"),
            @Mapping(target = "takenAt", source = "takenAt", dateFormat = "yyyy-MM-dd")

    })
    PublicMediaDto toDto(MediaAssetEntity entity);
}
