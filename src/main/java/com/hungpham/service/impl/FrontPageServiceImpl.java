package com.hungpham.service.impl;

import com.hungpham.common.exception.BadRequestException;
import com.hungpham.common.exception.EntityNotFoundException;
import com.hungpham.entity.FrontPageItemEntity;
import com.hungpham.entity.PostEntity;
import com.hungpham.repository.FrontPageItemRepository;
import com.hungpham.repository.PostRepository;
import com.hungpham.requests.UpdateFrontPageItemRequest;
import com.hungpham.service.FrontPageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hungpham.mappers.UuidBinaryMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FrontPageServiceImpl implements FrontPageService {

    private static final Logger log = LoggerFactory.getLogger(FrontPageServiceImpl.class);

    @Autowired
    private FrontPageItemRepository frontPageItemRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UuidBinaryMapper uuidBinaryMapper;

    @Override
    @Transactional
    public FrontPageItemEntity setFeatured(String postId, String actorUserId) {
        log.info("[FrontPage][SetFeatured] postId={}, actorUserId={}", postId, actorUserId);

        if (postId == null || postId.trim().isEmpty()) {
            throw new BadRequestException("postId is required");
        }

        PostEntity post = postRepository.findById(uuidBinaryMapper.toBytes(postId.trim()))
                .orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));

        // Rule: chỉ 1 featured
        int cleared = frontPageItemRepository.clearPinned();
        log.debug("[FrontPage][SetFeatured] cleared pinned count={}", cleared);

        FrontPageItemEntity item = frontPageItemRepository.findByPost(post)
                .orElseGet(() -> {
                    FrontPageItemEntity f = new FrontPageItemEntity();
                    f.setPost(post);
                    f.setPosition(1);
                    f.setActive(true);
                    f.setPinned(false);
                    f.setCreatedAt(LocalDateTime.now());
                    return f;
                });

        item.setPinned(true);
        item.setActive(true);
        item.setUpdatedAt(LocalDateTime.now());

        FrontPageItemEntity saved = frontPageItemRepository.save(item);
        log.info("[FrontPage][SetFeatured] done itemId={}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public FrontPageItemEntity upsertCurated(String postId,
                                             int position,
                                             boolean active,
                                             LocalDateTime startAt,
                                             LocalDateTime endAt,
                                             String note,
                                             String actorUserId) {
        log.info("[FrontPage][UpsertCurated] postId={}, pos={}, active={}, actorUserId={}",
                postId, position, active, actorUserId);

        if (postId == null || postId.trim().isEmpty()) {
            throw new BadRequestException("postId is required");
        }
        if (position <= 0) {
            throw new BadRequestException("position must be > 0");
        }
        if (startAt != null && endAt != null && endAt.isBefore(startAt)) {
            throw new BadRequestException("endAt must be >= startAt");
        }

        PostEntity post = postRepository.findById(uuidBinaryMapper.toBytes(postId.trim()))
                .orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));

        FrontPageItemEntity item = frontPageItemRepository.findByPost(post)
                .orElseGet(() -> {
                    FrontPageItemEntity f = new FrontPageItemEntity();
                    f.setPost(post);
                    f.setCreatedAt(LocalDateTime.now());
                    return f;
                });

        // Curated item: pinned=false (featured là pinned=true)
        item.setPinned(false);
        item.setPosition(position);
        item.setActive(active);
        item.setStartAt(startAt);
        item.setEndAt(endAt);
        item.setNote(note);
        item.setUpdatedAt(LocalDateTime.now());

        FrontPageItemEntity saved = frontPageItemRepository.save(item);
        log.debug("[FrontPage][UpsertCurated] done itemId={}", saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FrontPageItemEntity> getActiveItemsForNow() {
        LocalDateTime now = LocalDateTime.now();
        log.info("[FrontPage][GetActiveItems] now={}", now);
        List<FrontPageItemEntity> items = frontPageItemRepository.findActiveFrontPageItems(now);
        log.debug("[FrontPage][GetActiveItems] count={}", items.size());
        return items;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FrontPageItemEntity> getAllItemsForAdmin() {
        log.info("[FrontPage][AdminList]");
        return frontPageItemRepository.findAllOrderByPinnedDescPositionAsc();
    }

    @Override
    @Transactional
    public FrontPageItemEntity updateItem(Long id, UpdateFrontPageItemRequest req, String actorUserId) {
        log.info("[FrontPage][UpdateItem] id={}, actor={}", id, actorUserId);

        if (id == null) throw new BadRequestException("id is required");
        if (req == null) throw new BadRequestException("body is required");
        if (req.getStartAt() != null && req.getEndAt() != null && req.getEndAt().isBefore(req.getStartAt())) {
            throw new BadRequestException("endAt must be >= startAt");
        }

        FrontPageItemEntity item = frontPageItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FrontPageItem not found: " + id));

        // Nếu set pinned=true => clear pinned cũ (rule: chỉ 1 featured)
        if (Boolean.TRUE.equals(req.getPinned())) {
            frontPageItemRepository.clearPinned();
            item.setPinned(true);
        } else if (Boolean.FALSE.equals(req.getPinned())) {
            item.setPinned(false);
        }

        if (req.getActive() != null) item.setActive(req.getActive());
        if (req.getPosition() != null && req.getPosition() > 0) item.setPosition(req.getPosition());

        if (req.getStartAt() != null || req.getEndAt() != null) {
            item.setStartAt(req.getStartAt());
            item.setEndAt(req.getEndAt());
        }

        if (req.getNote() != null) item.setNote(req.getNote());

        item.setUpdatedAt(LocalDateTime.now());
        return frontPageItemRepository.save(item);
    }

    @Override
    @Transactional
    public void deleteItem(Long id, String actorUserId) {
        log.info("[FrontPage][DeleteItem] id={}, actor={}", id, actorUserId);
        if (id == null) throw new BadRequestException("id is required");

        FrontPageItemEntity item = frontPageItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FrontPageItem not found: " + id));

        frontPageItemRepository.delete(item);
    }

    @Override
    @Transactional
    public void reorder(List<Long> orderedIds, String actorUserId) {
        log.info("[FrontPage][Reorder] size={}, actor={}",
                orderedIds == null ? 0 : orderedIds.size(), actorUserId);

        if (orderedIds == null || orderedIds.isEmpty()) {
            throw new BadRequestException("orderedIds is required");
        }

        // position bắt đầu từ 1
        int pos = 1;
        for (Long id : orderedIds) {
            if (id == null) continue;
            FrontPageItemEntity item = frontPageItemRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("FrontPageItem not found: " + id));
            item.setPosition(pos++);
            item.setUpdatedAt(LocalDateTime.now());
            frontPageItemRepository.save(item);
        }
    }
}
