package com.hungpham.mappers;

import com.hungpham.dtos.NewUpTopDto;
import com.hungpham.entity.NewUpTopEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NewUpTopMapper {
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    NewUpTopEntity toEntity(NewUpTopDto newsDto);
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    NewUpTopDto toDto(NewUpTopEntity newsEntity);
}
