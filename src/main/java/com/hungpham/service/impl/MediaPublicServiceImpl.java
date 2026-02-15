package com.hungpham.service.impl;

import com.hungpham.common.enums.MediaKindEnum;
import com.hungpham.common.exception.BadRequestException;
import com.hungpham.common.exception.EntityNotFoundException;
import com.hungpham.dtos.MediaBinaryDto;
import com.hungpham.dtos.PublicMediaDto;
import com.hungpham.entity.MediaAssetEntity;
import com.hungpham.mappers.PublicMediaMapper;
import com.hungpham.mappers.UuidBinaryMapper;
import com.hungpham.repository.MediaAssetRepository;
import com.hungpham.service.MediaAdminService;
import com.hungpham.service.MediaPublicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class MediaPublicServiceImpl implements MediaPublicService {

    @Autowired private MediaAssetRepository mediaAssetRepository;
    @Autowired private PublicMediaMapper publicMediaMapper;
    @Autowired private UuidBinaryMapper uuidBinaryMapper;
    @Autowired private MediaAdminService mediaAdminService;

    @Override
    public Page<PublicMediaDto> getGallery(MediaKindEnum kind, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 24;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));

        Page<MediaAssetEntity> entities = (kind == null)
                ? mediaAssetRepository.findByActiveTrue(pageable)
                : mediaAssetRepository.findByActiveTrueAndKind(kind, pageable);

        return entities.map(publicMediaMapper::toDto);
    }

    @Override
    public PublicMediaDto getActiveById(String id) {
        MediaAssetEntity entity = mustGetActiveMedia(id);
        return publicMediaMapper.toDto(entity);
    }

    @Override
    public MediaBinaryDto loadActiveBinary(String id) {
        mustGetActiveMedia(id);
        return mediaAdminService.loadBinaryForAdmin(id);
    }

    private MediaAssetEntity mustGetActiveMedia(String id) {
        if (id == null || id.trim().isEmpty()) throw new BadRequestException("id is required");

        byte[] binId = uuidBinaryMapper.toBytes(id);
        if (binId == null) throw new BadRequestException("id is invalid UUID");

        MediaAssetEntity entity = mediaAssetRepository.findById(binId)
                .orElseThrow(() -> new EntityNotFoundException("Media not found: " + id));

        if (!entity.isActive()) throw new EntityNotFoundException("Media not found: " + id);
        return entity;
    }
}
