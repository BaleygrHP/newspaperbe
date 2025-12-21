package com.hungpham.service.impl;

import com.hungpham.common.enums.SectionVisibilityEnum;
import com.hungpham.common.exception.BadRequestException;
import com.hungpham.common.exception.EntityNotFoundException;
import com.hungpham.dtos.SectionDto;
import com.hungpham.entity.SectionEntity;
import com.hungpham.mappers.SectionMapper;
import com.hungpham.mappers.UuidBinaryMapper;
import com.hungpham.repository.SectionRepository;
import com.hungpham.requests.section.CreateSectionRequest;
import com.hungpham.requests.section.UpdateSectionRequest;
import com.hungpham.service.SectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SectionServiceImpl implements SectionService {

    private static final Logger log = LoggerFactory.getLogger(SectionServiceImpl.class);

    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private SectionMapper sectionMapper;

    @Autowired
    private UuidBinaryMapper uuidBinaryMapper;

    // ===== Public =====
    @Override
    @Transactional(readOnly = true)
    public List<SectionDto> getPublicMenu() {
        log.info("[Section][PublicMenu] load");
        List<SectionEntity> entities =
                sectionRepository.findByIsActiveTrueAndVisibilityOrderBySortOrderAsc(
                        SectionVisibilityEnum.PUBLIC
                );

        List<SectionDto> dtos = new ArrayList<>();
        for (SectionEntity e : entities) dtos.add(sectionMapper.toDto(e));

        log.debug("[Section][PublicMenu] count={}", dtos.size());
        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SectionDto> getAllActive() {
        log.info("[Section][AllActive] load");
        List<SectionEntity> entities = sectionRepository.findByIsActiveTrueOrderBySortOrderAsc();

        List<SectionDto> dtos = new ArrayList<>();
        for (SectionEntity e : entities) dtos.add(sectionMapper.toDto(e));

        log.debug("[Section][AllActive] count={}", dtos.size());
        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public SectionDto getByKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new BadRequestException("key is required");
        }
        log.info("[Section][GetByKey] key={}", key);

        SectionEntity entity = sectionRepository.findByKeyAndIsActiveTrue(key.trim())
                .orElseThrow(() -> new EntityNotFoundException("Section not found: " + key));

        return sectionMapper.toDto(entity);
    }

    // ===== Admin =====
    @Override
    @Transactional(readOnly = true)
    public List<SectionDto> adminListAll(String q) {
        log.info("[Section][AdminList] q={}", q);

        List<SectionEntity> entities = sectionRepository.findAllByOrderBySortOrderAsc();
        List<SectionDto> dtos = new ArrayList<>();

        String keyword = (q == null) ? null : q.trim().toLowerCase();
        for (SectionEntity e : entities) {
            if (keyword != null && !keyword.isEmpty()) {
                String key = e.getKey() == null ? "" : e.getKey().toLowerCase();
                String name = e.getName() == null ? "" : e.getName().toLowerCase();
                if (!key.contains(keyword) && !name.contains(keyword)) continue;
            }
            dtos.add(sectionMapper.toDto(e));
        }

        log.debug("[Section][AdminList] count={}", dtos.size());
        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public SectionDto adminGetById(String id) {
        if (id == null || id.trim().isEmpty()) throw new BadRequestException("id is required");
        log.info("[Section][AdminGetById] id={}", id);

        SectionEntity entity = sectionRepository.findById(uuidBinaryMapper.toBytes(id.trim()))
                .orElseThrow(() -> new EntityNotFoundException("Section not found: " + id));

        return sectionMapper.toDto(entity);
    }

    @Override
    @Transactional
    public SectionDto adminCreate(CreateSectionRequest req, String actorUserId) {
        log.info("[Section][AdminCreate] actor={}", actorUserId);

        if (req == null) throw new BadRequestException("body is required");
        if (req.getKey() == null || req.getKey().trim().isEmpty()) throw new BadRequestException("key is required");
        if (req.getName() == null || req.getName().trim().isEmpty()) throw new BadRequestException("name is required");
        if (req.getVisibility() == null) throw new BadRequestException("visibility is required");

        String key = req.getKey().trim().toLowerCase();
        if (sectionRepository.existsByKey(key)) {
            throw new BadRequestException("Section key already exists: " + key);
        }

        SectionEntity sectionEntity = new SectionEntity();
        sectionEntity.setId(uuidBinaryMapper.newUuidBytes());
        sectionEntity.setKey(key);
        sectionEntity.setName(req.getName().trim());
        sectionEntity.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        sectionEntity.setActive(req.getActive() != null ? req.getActive() : true);
        sectionEntity.setVisibility(req.getVisibility());
        sectionEntity.setCreatedDate(LocalDateTime.now());
        sectionEntity.setUpdatedDate(LocalDateTime.now());

        return sectionMapper.toDto(sectionRepository.save(sectionEntity));
    }

    @Override
    @Transactional
    public SectionDto adminUpdate(String id, UpdateSectionRequest req, String actorUserId) {
        if (id == null || id.trim().isEmpty()) throw new BadRequestException("id is required");
        if (req == null) throw new BadRequestException("body is required");
        log.info("[Section][AdminUpdate] id={}, actor={}", id, actorUserId);

        SectionEntity sectionEntity = sectionRepository.findById(uuidBinaryMapper.toBytes(id.trim()))
                .orElseThrow(() -> new EntityNotFoundException("Section not found: " + id));

        if (req.getName() != null) sectionEntity.setName(req.getName().trim());
        if (req.getSortOrder() != null) sectionEntity.setSortOrder(req.getSortOrder());
        if (req.getActive() != null) sectionEntity.setActive(req.getActive());
        if (req.getVisibility() != null) sectionEntity.setVisibility(req.getVisibility());

        sectionEntity.setUpdatedDate(LocalDateTime.now());
        return sectionMapper.toDto(sectionRepository.save(sectionEntity));
    }

    @Override
    @Transactional
    public SectionDto adminToggleActive(String id, String actorUserId) {
        if (id == null || id.trim().isEmpty()) throw new BadRequestException("id is required");
        log.info("[Section][AdminToggleActive] id={}, actor={}", id, actorUserId);

        SectionEntity sectionEntity = sectionRepository.findById(uuidBinaryMapper.toBytes(id.trim()))
                .orElseThrow(() -> new EntityNotFoundException("Section not found: " + id));

        sectionEntity.setActive(!sectionEntity.isActive());
        sectionEntity.setUpdatedDate(LocalDateTime.now());
        return sectionMapper.toDto(sectionRepository.save(sectionEntity));
    }
}
