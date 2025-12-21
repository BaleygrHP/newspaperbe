package com.hungpham.mappers;

import com.hungpham.dtos.PostMediaDto;
import com.hungpham.entity.PostMediaEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UuidBinaryMapper.class})
public interface PostMediaMapper {

    @Mappings({
            @Mapping(target = "postId", source = "post.id", qualifiedByName = "toUuid"),
            @Mapping(target = "mediaId", source = "media.id", qualifiedByName = "toUuid")
    })
    PostMediaDto toDto(PostMediaEntity entity);

    @Mappings({
            // post/media set ở service (load PostEntity + MediaAssetEntity)
            @Mapping(target = "post", ignore = true),
            @Mapping(target = "media", ignore = true),

            // id (embedded) set ở service (vì cần 2 id)
            @Mapping(target = "id", ignore = true)
    })
    PostMediaEntity toEntity(PostMediaDto dto);
}
