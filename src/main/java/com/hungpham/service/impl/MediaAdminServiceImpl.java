package com.hungpham.service.impl;

import com.hungpham.common.enums.*;
import com.hungpham.common.exception.BadRequestException;
import com.hungpham.common.exception.EntityNotFoundException;
import com.hungpham.dtos.MediaAssetDto;
import com.hungpham.entity.AuditLogEntity;
import com.hungpham.entity.MediaAssetEntity;
import com.hungpham.entity.UserEntity;
import com.hungpham.mappers.MediaAssetMapper;
import com.hungpham.mappers.UuidBinaryMapper;
import com.hungpham.repository.AuditLogRepository;
import com.hungpham.repository.MediaAssetRepository;
import com.hungpham.repository.UserRepository;
import com.hungpham.requests.media.CreateMediaByUrlRequest;
import com.hungpham.requests.media.UpdateMediaRequest;
import com.hungpham.service.MediaAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MediaAdminServiceImpl implements MediaAdminService {

    @Autowired private MediaAssetRepository mediaAssetRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private MediaAssetMapper mediaAssetMapper;
    @Autowired private UuidBinaryMapper uuidBinaryMapper;

    @Override
    public Page<MediaAssetDto> search(MediaKindEnum kind, Boolean active, String category, String q, int page, int size) {
        String nq = normalizeQuery(q);
        String ncat = normalizeQuery(category);

        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "createdDate")
        );

        Page<MediaAssetEntity> p = mediaAssetRepository.adminSearch(kind, active, ncat, nq, pageable);
        return p.map(mediaAssetMapper::toDto);
    }

    @Override
    public MediaAssetDto getById(String id) {
        MediaAssetEntity e = mustGetMedia(id);
        return mediaAssetMapper.toDto(e);
    }

    @Transactional
    @Override
    public MediaAssetDto createByUrl(CreateMediaByUrlRequest req, String actorUserId) {
        UserEntity actor = mustGetUser(actorUserId);

        if (req == null) throw new BadRequestException("request is required");
        if (isEmpty(req.getUrl())) throw new BadRequestException("url is required");
        if (isEmpty(req.getMimeType())) throw new BadRequestException("mimeType is required");

        if (!isEmpty(req.getFileHash()) && mediaAssetRepository.existsByFileHash(req.getFileHash())) {
            throw new BadRequestException("fileHash already exists");
        }

        MediaAssetEntity e = new MediaAssetEntity();
        e.setId(uuidBinaryMapper.newUuidBytes());
        e.setOwner(actor);
        e.setKind(req.getKind() == null ? MediaKindEnum.IMAGE : req.getKind());
        e.setStorage(req.getStorage() == null ? MediaStorageEnum.URL : req.getStorage());
        e.setUrl(req.getUrl());
        e.setMimeType(req.getMimeType());
        e.setByteSize(req.getByteSize() == null ? 0L : req.getByteSize());

        e.setWidth(req.getWidth());
        e.setHeight(req.getHeight());
        e.setAlt(req.getAlt());
        e.setTitle(req.getTitle());

        e.setCaption(req.getCaption());
        e.setLocation(req.getLocation());
        e.setCategory(req.getCategory());

        if (!isEmpty(req.getTakenAt())) {
            e.setTakenAt(LocalDate.parse(req.getTakenAt())); // yyyy-MM-dd
        }

        e.setFileHash(req.getFileHash());
        e.setActive(true);

        mediaAssetRepository.save(e);
        insertAudit(actor, AuditActionEnum.CREATE, AuditEntityTypeEnum.MEDIA, e.getId());

        return mediaAssetMapper.toDto(e);
    }

    @Transactional
    @Override
    public MediaAssetDto update(String id, UpdateMediaRequest req, String actorUserId) {
        UserEntity actor = mustGetUser(actorUserId);
        MediaAssetEntity e = mustGetMedia(id);

        if (req == null) throw new BadRequestException("request is required");

        if (req.getAlt() != null) e.setAlt(req.getAlt());
        if (req.getTitle() != null) e.setTitle(req.getTitle());
        if (req.getCaption() != null) e.setCaption(req.getCaption());
        if (req.getLocation() != null) e.setLocation(req.getLocation());
        if (req.getCategory() != null) e.setCategory(req.getCategory());

        if (req.getTakenAt() != null) {
            if (isEmpty(req.getTakenAt())) {
                e.setTakenAt(null);
            } else {
                e.setTakenAt(LocalDate.parse(req.getTakenAt())); // yyyy-MM-dd
            }
        }

        if (req.getActive() != null) e.setActive(req.getActive());

        mediaAssetRepository.save(e);
        insertAudit(actor, AuditActionEnum.UPDATE, AuditEntityTypeEnum.MEDIA, e.getId());

        return mediaAssetMapper.toDto(e);
    }

    @Transactional
    @Override
    public void disable(String id, String actorUserId) {
        UserEntity actor = mustGetUser(actorUserId);
        MediaAssetEntity e = mustGetMedia(id);

        e.setActive(false);
        mediaAssetRepository.save(e);

        insertAudit(actor, AuditActionEnum.DELETE, AuditEntityTypeEnum.MEDIA, e.getId());
    }

    @Override
    public List<String> listCategories() {
        return mediaAssetRepository.listDistinctCategories();
    }

    // =========================
    // Helpers
    // =========================
    private UserEntity mustGetUser(String userId) {
        if (isEmpty(userId)) throw new BadRequestException("actorUserId is required");
        byte[] uid = uuidBinaryMapper.toBytes(userId);
        return userRepository.findById(uid)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    private MediaAssetEntity mustGetMedia(String id) {
        if (isEmpty(id)) throw new BadRequestException("id is required");
        byte[] mid = uuidBinaryMapper.toBytes(id);
        return mediaAssetRepository.findById(mid)
                .orElseThrow(() -> new EntityNotFoundException("Media not found: " + id));
    }

    private void insertAudit(UserEntity actor,
                             AuditActionEnum action,
                             AuditEntityTypeEnum entityType,
                             byte[] entityId) {
        AuditLogEntity logEntity = new AuditLogEntity();
        logEntity.setActor(actor);
        logEntity.setAction(action);
        logEntity.setEntityType(entityType);
        logEntity.setEntityId(entityId);
        logEntity.setCreatedDate(LocalDateTime.now());
        auditLogRepository.save(logEntity);
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String normalizeQuery(String q) {
        if (q == null) return null;
        String t = q.trim();
        return t.isEmpty() ? null : t;
    }
}
