package com.hungpham.mappers;

import com.hungpham.dtos.FrontPageItemDto;
import com.hungpham.dtos.FrontPageSupportingItemDto;
import com.hungpham.dtos.PostPreviewDto;
import com.hungpham.dtos.SectionDto;
import com.hungpham.entity.FrontPageItemEntity;
import com.hungpham.entity.PostEntity;
import com.hungpham.entity.SectionEntity;
import org.springframework.stereotype.Component;

@Component
public class FrontPageItemMapper {

    private final UuidBinaryMapper uuidBinaryMapper;
    private final DateTimeMapper dateTimeMapper;

    public FrontPageItemMapper(UuidBinaryMapper uuidBinaryMapper, DateTimeMapper dateTimeMapper) {
        this.uuidBinaryMapper = uuidBinaryMapper;
        this.dateTimeMapper = dateTimeMapper;
    }

    public FrontPageItemDto toDto(FrontPageItemEntity entity) {
        if (entity == null) return null;

        FrontPageItemDto dto = new FrontPageItemDto();
        dto.setId(entity.getId().toString());
        dto.setPostId(entity.getPost() == null ? null : uuidBinaryMapper.toUuid(entity.getPost().getId()));
        dto.setPosition(entity.getPosition());
        dto.setPinned(entity.isPinned());
        dto.setActive(entity.isActive());
        dto.setStartAt(dateTimeMapper.ldtToString(entity.getStartAt()));
        dto.setEndAt(dateTimeMapper.ldtToString(entity.getEndAt()));
        dto.setNote(entity.getNote());

        dto.setPost(toPostPreview(entity.getPost()));
        return dto;
    }

    public FrontPageSupportingItemDto toSupportingDto(FrontPageItemEntity entity) {
        if (entity == null) return null;

        FrontPageSupportingItemDto dto = new FrontPageSupportingItemDto();
        dto.setId(entity.getId());
        dto.setPostId(entity.getPost() == null ? null : uuidBinaryMapper.toUuid(entity.getPost().getId()));
        dto.setPosition(entity.getPosition());
        dto.setPinned(entity.isPinned());
        dto.setActive(entity.isActive());
        dto.setStartAt(dateTimeMapper.ldtToString(entity.getStartAt()));
        dto.setEndAt(dateTimeMapper.ldtToString(entity.getEndAt()));
        dto.setNote(entity.getNote());
        dto.setPost(toPostPreview(entity.getPost()));
        return dto;
    }

    private PostPreviewDto toPostPreview(PostEntity post) {
        if (post == null) return null;

        PostPreviewDto dto = new PostPreviewDto();
        dto.setId(uuidBinaryMapper.toUuid(post.getId()));
        dto.setTitle(post.getTitle());
        dto.setSlug(post.getSlug());
        dto.setSection(toSectionDto(post.getSection()));
        return dto;
    }

    private SectionDto toSectionDto(SectionEntity section) {
        if (section == null) return null;

        SectionDto dto = new SectionDto();
        dto.setId(uuidBinaryMapper.toUuid(section.getId()));
        dto.setKey(section.getKey());
        dto.setName(section.getName());
        return dto;
    }
}

