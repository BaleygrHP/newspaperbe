package com.hungpham.mappers;

import com.hungpham.dtos.FrontPageItemDto;
import com.hungpham.dtos.PostPreviewDto;
import com.hungpham.dtos.SectionDto;
import com.hungpham.entity.FrontPageItemEntity;
import com.hungpham.entity.PostEntity;
import com.hungpham.entity.SectionEntity;
import org.springframework.stereotype.Component;

@Component
public class FrontPageItemMapper {

    public FrontPageItemDto toDto(FrontPageItemEntity entity) {
        if (entity == null) return null;

        FrontPageItemDto dto = new FrontPageItemDto();
        dto.setId(entity.getId().toString());
        dto.setPosition(entity.getPosition());
        dto.setPinned(entity.isPinned());
        dto.setActive(entity.isActive());

        dto.setPost(toPostPreview(entity.getPost()));
        return dto;
    }

    private PostPreviewDto toPostPreview(PostEntity post) {
        if (post == null) return null;

        PostPreviewDto dto = new PostPreviewDto();
        dto.setId(post.getId().toString());
        dto.setTitle(post.getTitle());
        dto.setSlug(post.getSlug());
        dto.setSection(toSectionDto(post.getSection()));
        return dto;
    }

    private SectionDto toSectionDto(SectionEntity section) {
        if (section == null) return null;

        SectionDto dto = new SectionDto();
        dto.setId(section.getId().toString());
        dto.setKey(section.getKey());
        dto.setName(section.getName());
        return dto;
    }
}

