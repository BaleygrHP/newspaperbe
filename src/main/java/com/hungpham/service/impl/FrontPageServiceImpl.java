package com.hungpham.service.impl;

import com.hungpham.common.enums.PostStatusEnum;
import com.hungpham.common.exception.BadRequestException;
import com.hungpham.common.exception.ConflictException;
import com.hungpham.common.exception.EntityNotFoundException;
import com.hungpham.dtos.FrontPageCompositionDto;
import com.hungpham.dtos.FrontPageItemDto;
import com.hungpham.dtos.FrontPageSupportingItemDto;
import com.hungpham.dtos.PostPreviewDto;
import com.hungpham.entity.FrontPageItemEntity;
import com.hungpham.entity.PostEntity;
import com.hungpham.mappers.DateTimeMapper;
import com.hungpham.mappers.FrontPageItemMapper;
import com.hungpham.mappers.UuidBinaryMapper;
import com.hungpham.repository.FrontPageItemRepository;
import com.hungpham.repository.PostRepository;
import com.hungpham.requests.UpdateFrontPageItemRequest;
import com.hungpham.service.FrontPageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class FrontPageServiceImpl implements FrontPageService {

    private static final Logger log = LoggerFactory.getLogger(FrontPageServiceImpl.class);
    private static final int SUPPORTING_MAX_ITEMS = 6;
    private static final String COMPOSITION_STATUS = "SAVED";
    private static final String STALE_LAYOUT_MESSAGE = "Front page layout is stale. Please refresh.";

    @Autowired
    private FrontPageItemRepository frontPageItemRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UuidBinaryMapper uuidBinaryMapper;
    @Autowired
    private FrontPageItemMapper frontPageItemMapper;
    @Autowired
    private DateTimeMapper dateTimeMapper;

    @Override
    @Transactional(readOnly = true)
    public FrontPageCompositionDto getComposition() {
        List<FrontPageItemEntity> activeItems = frontPageItemRepository.findByActiveTrueOrderByPinnedDescPositionAsc();
        LocalDateTime maxUpdatedAt = frontPageItemRepository.findMaxUpdatedAt();

        FrontPageItemEntity featuredEntity = null;
        List<FrontPageSupportingItemDto> supporting = new ArrayList<FrontPageSupportingItemDto>();

        for (FrontPageItemEntity item : activeItems) {
            if (item.isPinned() && featuredEntity == null) {
                featuredEntity = item;
                continue;
            }
            if (!item.isPinned()) {
                supporting.add(frontPageItemMapper.toSupportingDto(item));
            }
        }

        PostPreviewDto featured = null;
        if (featuredEntity != null) {
            FrontPageSupportingItemDto mapped = frontPageItemMapper.toSupportingDto(featuredEntity);
            featured = mapped == null ? null : mapped.getPost();
        }

        FrontPageCompositionDto dto = new FrontPageCompositionDto();
        dto.setStatus(COMPOSITION_STATUS);
        dto.setVersion(toEpochMillis(maxUpdatedAt));
        dto.setUpdatedAt(dateTimeMapper.ldtToString(maxUpdatedAt));
        dto.setFeatured(featured);
        dto.setItems(supporting);
        return dto;
    }

    @Override
    @Transactional
    public FrontPageCompositionDto clearFeatured(String actorUserId, Long expectedVersion) {
        assertExpectedVersion(expectedVersion);

        Optional<FrontPageItemEntity> currentFeatured = frontPageItemRepository
                .findFirstByPinnedTrueAndActiveTrueOrderByUpdatedAtDescIdDesc();

        if (currentFeatured.isPresent()) {
            FrontPageItemEntity item = currentFeatured.get();
            item.setPinned(false);
            item.setActive(false);
            item.setUpdatedAt(LocalDateTime.now());
            frontPageItemRepository.save(item);
        }

        return getComposition();
    }

    @Override
    @Transactional
    public FrontPageItemDto setFeatured(String postId, String actorUserId, Long expectedVersion) {
        log.info("[FrontPage][SetFeatured] postId={}, actorUserId={}", postId, actorUserId);
        assertExpectedVersion(expectedVersion);

        PostEntity post = mustGetPublishedPost(postId);
        LocalDateTime now = LocalDateTime.now();

        Optional<FrontPageItemEntity> currentFeaturedOpt = frontPageItemRepository
                .findFirstByPinnedTrueAndActiveTrueOrderByUpdatedAtDescIdDesc();

        if (currentFeaturedOpt.isPresent()) {
            FrontPageItemEntity currentFeatured = currentFeaturedOpt.get();
            boolean samePost = Arrays.equals(currentFeatured.getPost().getId(), post.getId());
            if (!samePost) {
                currentFeatured.setPinned(false);
                currentFeatured.setActive(false);
                currentFeatured.setUpdatedAt(now);
                frontPageItemRepository.save(currentFeatured);
            }
        }

        FrontPageItemEntity item = frontPageItemRepository.findByPost(post)
                .orElseGet(() -> {
                    FrontPageItemEntity created = new FrontPageItemEntity();
                    created.setPost(post);
                    created.setPosition(1);
                    created.setCreatedAt(now);
                    return created;
                });

        item.setPinned(true);
        item.setActive(true);
        item.setPosition(1);
        item.setStartAt(null);
        item.setEndAt(null);
        item.setUpdatedAt(now);

        FrontPageItemEntity saved = frontPageItemRepository.save(item);
        return frontPageItemMapper.toDto(saved);
    }

    @Override
    @Transactional
    public FrontPageItemDto upsertCurated(String postId,
                                          int position,
                                          boolean active,
                                          LocalDateTime startAt,
                                          LocalDateTime endAt,
                                          String note,
                                          String actorUserId,
                                          Long expectedVersion) {
        log.info("[FrontPage][UpsertCurated] postId={}, pos={}, active={}, actorUserId={}",
                postId, position, active, actorUserId);
        assertExpectedVersion(expectedVersion);

        if (postId == null || postId.trim().isEmpty()) {
            throw new BadRequestException("postId is required");
        }
        if (position <= 0) {
            throw new BadRequestException("position must be > 0");
        }
        if (startAt != null && endAt != null && endAt.isBefore(startAt)) {
            throw new BadRequestException("endAt must be >= startAt");
        }

        PostEntity post = mustGetPublishedPost(postId);
        LocalDateTime now = LocalDateTime.now();

        Optional<FrontPageItemEntity> existingOpt = frontPageItemRepository.findByPost(post);
        FrontPageItemEntity existing = existingOpt.orElse(null);

        if (existing != null && existing.isPinned() && existing.isActive()) {
            throw new BadRequestException("Post is currently lead story");
        }

        if (existing != null && existing.isActive() && !existing.isPinned() && active) {
            throw new BadRequestException("Post is already in supporting");
        }

        boolean existingActiveSupporting = existing != null && existing.isActive() && !existing.isPinned();
        if (active && !existingActiveSupporting) {
            int activeSupportingCount = frontPageItemRepository
                    .findByActiveTrueAndPinnedFalseOrderByPositionAscIdAsc()
                    .size();
            if (activeSupportingCount >= SUPPORTING_MAX_ITEMS) {
                throw new BadRequestException("Supporting stories max is " + SUPPORTING_MAX_ITEMS);
            }
        }

        FrontPageItemEntity item = existing;
        if (item == null) {
            item = new FrontPageItemEntity();
            item.setPost(post);
            item.setCreatedAt(now);
        }

        item.setPinned(false);
        item.setPosition(position);
        item.setActive(active);
        item.setStartAt(startAt);
        item.setEndAt(endAt);
        item.setNote(note);
        item.setUpdatedAt(now);

        FrontPageItemEntity saved = frontPageItemRepository.save(item);
        return frontPageItemMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FrontPageItemEntity> getActiveItemsForNow() {
        LocalDateTime now = LocalDateTime.now();
        return frontPageItemRepository.findActiveFrontPageItems(now);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FrontPageItemDto> getAllItemsForAdmin() {
        List<FrontPageItemEntity> entities = frontPageItemRepository.findByActiveTrueOrderByPinnedDescPositionAsc();
        List<FrontPageItemDto> dtos = new ArrayList<FrontPageItemDto>(entities.size());
        for (FrontPageItemEntity entity : entities) {
            dtos.add(frontPageItemMapper.toDto(entity));
        }
        return dtos;
    }

    @Override
    @Transactional
    public FrontPageItemDto updateItem(Long id, UpdateFrontPageItemRequest req, String actorUserId, Long expectedVersion) {
        assertExpectedVersion(expectedVersion);

        if (id == null) throw new BadRequestException("id is required");
        if (req == null) throw new BadRequestException("body is required");
        if (req.getStartAt() != null && req.getEndAt() != null && req.getEndAt().isBefore(req.getStartAt())) {
            throw new BadRequestException("endAt must be >= startAt");
        }

        FrontPageItemEntity item = frontPageItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FrontPageItem not found: " + id));

        if (Boolean.TRUE.equals(req.getPinned())) {
            String postId = uuidBinaryMapper.toUuid(item.getPost().getId());
            return setFeatured(postId, actorUserId, expectedVersion);
        }

        if (req.getPinned() != null) {
            item.setPinned(req.getPinned());
        }

        if (req.getActive() != null) {
            if (Boolean.TRUE.equals(req.getActive()) && !item.isActive() && !item.isPinned()) {
                int activeSupportingCount = frontPageItemRepository
                        .findByActiveTrueAndPinnedFalseOrderByPositionAscIdAsc()
                        .size();
                if (activeSupportingCount >= SUPPORTING_MAX_ITEMS) {
                    throw new BadRequestException("Supporting stories max is " + SUPPORTING_MAX_ITEMS);
                }
            }
            item.setActive(req.getActive());
        }

        if (req.getPosition() != null) {
            if (req.getPosition() <= 0) throw new BadRequestException("position must be > 0");
            item.setPosition(req.getPosition());
        }

        if (req.getStartAt() != null || req.getEndAt() != null) {
            item.setStartAt(req.getStartAt());
            item.setEndAt(req.getEndAt());
        }

        if (req.getNote() != null) {
            item.setNote(req.getNote());
        }

        if (!item.isActive()) {
            item.setPinned(false);
        }

        item.setUpdatedAt(LocalDateTime.now());
        FrontPageItemEntity saved = frontPageItemRepository.save(item);
        return frontPageItemMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteItem(Long id, String actorUserId, Long expectedVersion) {
        assertExpectedVersion(expectedVersion);
        if (id == null) throw new BadRequestException("id is required");

        FrontPageItemEntity item = frontPageItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FrontPageItem not found: " + id));

        item.setActive(false);
        item.setPinned(false);
        item.setUpdatedAt(LocalDateTime.now());
        frontPageItemRepository.save(item);
    }

    @Override
    @Transactional
    public void reorder(List<Long> orderedIds, String actorUserId, Long expectedVersion) {
        assertExpectedVersion(expectedVersion);

        if (orderedIds == null || orderedIds.isEmpty()) {
            throw new BadRequestException("orderedIds is required");
        }

        List<FrontPageItemEntity> activeSupporting = frontPageItemRepository
                .findByActiveTrueAndPinnedFalseOrderByPositionAscIdAsc();
        if (activeSupporting.isEmpty()) {
            throw new BadRequestException("No active supporting stories to reorder");
        }

        Set<Long> uniqueIds = new LinkedHashSet<Long>();
        for (Long id : orderedIds) {
            if (id == null) throw new BadRequestException("orderedIds cannot contain null");
            if (!uniqueIds.add(id)) throw new BadRequestException("orderedIds contains duplicate id: " + id);
        }

        Set<Long> expectedIds = new LinkedHashSet<Long>();
        Map<Long, FrontPageItemEntity> itemsById = new LinkedHashMap<Long, FrontPageItemEntity>();
        for (FrontPageItemEntity item : activeSupporting) {
            expectedIds.add(item.getId());
            itemsById.put(item.getId(), item);
        }

        if (uniqueIds.size() != expectedIds.size()
                || !uniqueIds.containsAll(expectedIds)
                || !expectedIds.containsAll(uniqueIds)) {
            throw new BadRequestException("orderedIds must match all active supporting items");
        }

        LocalDateTime now = LocalDateTime.now();
        int position = 1;
        for (Long id : orderedIds) {
            FrontPageItemEntity item = itemsById.get(id);
            if (item == null) throw new EntityNotFoundException("FrontPageItem not found: " + id);
            item.setPosition(position++);
            item.setUpdatedAt(now);
            frontPageItemRepository.save(item);
        }
    }

    private PostEntity mustGetPublishedPost(String postId) {
        if (postId == null || postId.trim().isEmpty()) {
            throw new BadRequestException("postId is required");
        }

        PostEntity post = postRepository.findById(uuidBinaryMapper.toBytes(postId.trim()))
                .orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));

        if (post.getDeletedAt() != null) {
            throw new BadRequestException("Post is deleted");
        }
        if (post.getStatus() != PostStatusEnum.PUBLISHED) {
            throw new BadRequestException("Post must be PUBLISHED");
        }
        return post;
    }

    private void assertExpectedVersion(Long expectedVersion) {
        if (expectedVersion == null) {
            throw new BadRequestException("front page version is required");
        }

        Long currentVersion = resolveCurrentVersion();
        if (!Objects.equals(currentVersion, expectedVersion)) {
            throw new ConflictException(STALE_LAYOUT_MESSAGE);
        }
    }

    private Long resolveCurrentVersion() {
        return toEpochMillis(frontPageItemRepository.findMaxUpdatedAt());
    }

    private Long toEpochMillis(LocalDateTime dateTime) {
        if (dateTime == null) return 0L;
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
