package com.hungpham.mappers;

import com.hungpham.dtos.PostRevisionDto;
import com.hungpham.entity.PostRevisionEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UuidBinaryMapper.class, DateTimeMapper.class})
public interface PostRevisionMapper {

    @Mappings({
            @Mapping(target = "id", source = "id", qualifiedByName = "toUuid"),
            @Mapping(target = "createdDate", source = "createdDate", qualifiedByName = "ldtToString")
    })
    PostRevisionDto toDto(PostRevisionEntity entity);

    @Mappings({
            @Mapping(target = "id", source = "id", qualifiedByName = "toBytes"),
            @Mapping(target = "post", ignore = true),   // set ở service
            @Mapping(target = "editor", ignore = true), // set ở service
            @Mapping(target = "createdDate", source = "createdDate", qualifiedByName = "stringToLdt")
    })
    PostRevisionEntity toEntity(PostRevisionDto dto);
}
