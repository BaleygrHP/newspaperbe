package com.hungpham.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hungpham.common.enums.AuditActionEnum;
import com.hungpham.common.enums.AuditEntityTypeEnum;
import com.hungpham.common.enums.PostStatusEnum;
import com.hungpham.common.exception.BadRequestException;
import com.hungpham.common.exception.EntityNotFoundException;
import com.hungpham.common.util.SlugUtil;
import com.hungpham.dtos.PostDto;
import com.hungpham.entity.*;
import com.hungpham.mappers.PostMapper;
import com.hungpham.mappers.UuidBinaryMapper;
import com.hungpham.repository.*;
import com.hungpham.requests.CreatePostRequest;
import com.hungpham.requests.UpdatePostRequest;
import com.hungpham.service.PostAdminService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostAdminServiceImpl implements PostAdminService {

    private static final Logger log = LoggerFactory.getLogger(PostAdminServiceImpl.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRevisionRepository postRevisionRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private FrontPageItemRepository frontPageItemRepository;

    @Autowired
    private PostMapper postMapper;
    @Autowired
    private UuidBinaryMapper uuidBinaryMapper;

    // =========================
    // CREATE DRAFT
    // =========================
    @Override
    @Transactional
    public PostDto createDraft(CreatePostRequest req, String actorUserId) {
        log.info("[AdminPost][CreateDraft] actorUserId={}", actorUserId);

        if (req == null)
            throw new BadRequestException("Body is required");
        if (isEmpty(req.getTitle()))
            throw new BadRequestException("Title is required");
        if (isEmpty(req.getSectionId()) && isEmpty(req.getSection()))
            throw new BadRequestException("Section is required");
        if (req.getContentJson() == null && req.getContent() == null)
            throw new BadRequestException("content is required");

        UserEntity actor = mustGetUser(actorUserId);

        SectionEntity section;
        if (!isEmpty(req.getSection())) {
            String sectionKey = req.getSection().trim().toLowerCase();
            section = sectionRepository.findByKey(sectionKey)
                    .orElseThrow(() -> new EntityNotFoundException("Section not found: " + sectionKey));
        } else {
            section = mustGetSection(req.getSectionId());
        }

        PostEntity post = new PostEntity();
        post.setId(uuidBinaryMapper.newUuidBytes());
        post.setAuthor(actor);
        post.setSection(section);

        post.setTitle(req.getTitle().trim());
        post.setSubtitle(req.getSubtitle());
        post.setExcerpt(req.getExcerpt());
        post.setCoverImageUrl(req.getCoverImageUrl());

        // slug
        String baseSlug = isEmpty(req.getSlug())
                ? SlugUtil.slugIfy(req.getTitle())
                : req.getSlug().trim();
        post.setSlug(ensureUniqueSlug(baseSlug, null));

        post.setStatus(PostStatusEnum.DRAFT);

        String normalizedContentJson = resolveContentJson(req.getContentJson(), req.getContent());
        post.setContentJson(normalizedContentJson);
        if (!isEmpty(req.getContentMd())) {
            post.setContentMd(req.getContentMd());
        } else if (!isEmpty(req.getContent())) {
            // Keep markdown/text editor content in a readable field for FE round-trip.
            post.setContentMd(req.getContent());
        }
        post.setContentHtml(req.getContentHtml());
        post.setContentText(req.getContentText());
        post.setCreatedDate(LocalDateTime.now());
        post.setUpdatedDate(LocalDateTime.now());
        log.info("[AdminPost][CreateDraft] created postId={}, {}", post.getCreatedDate(), post.getUpdatedDate());
        PostEntity saved = postRepository.save(post);

        insertRevision(saved, actor, "CREATE_DRAFT");
        insertAudit(actor, AuditActionEnum.CREATE, AuditEntityTypeEnum.POST, saved.getId(), null);

        log.info("[AdminPost][CreateDraft] created postId={}", uuidBinaryMapper.toUuid(saved.getId()));
        return postMapper.toDto(saved);
    }

    // =========================
    // UPDATE POST (draft/published tuỳ policy)
    // =========================
    @Override
    @Transactional
    public PostDto updatePost(String postId, UpdatePostRequest req, String actorUserId) {
        log.info("[AdminPost][Update] postId={}, actorUserId={}", postId, actorUserId);

        if (isEmpty(postId))
            throw new BadRequestException("postId is required");
        if (req == null)
            throw new BadRequestException("Body is required");

        UserEntity actor = mustGetUser(actorUserId);
        PostEntity post = mustGetPost(postId);

        if (post.getDeletedAt() != null)
            throw new BadRequestException("Post was deleted");

        // update fields (partial)
        if (!isEmpty(req.getTitle()))
            post.setTitle(req.getTitle().trim());
        if (req.getSubtitle() != null)
            post.setSubtitle(req.getSubtitle());
        if (req.getExcerpt() != null)
            post.setExcerpt(req.getExcerpt());
        if (req.getCoverImageUrl() != null)
            post.setCoverImageUrl(req.getCoverImageUrl());

        if (!isEmpty(req.getSection())) {
            String sectionKey = req.getSection().trim().toLowerCase();
            SectionEntity section = sectionRepository.findByKey(sectionKey)
                    .orElseThrow(() -> new EntityNotFoundException("Section not found: " + sectionKey));
            post.setSection(section);
        } else if (!isEmpty(req.getSectionId())) {
            SectionEntity section = mustGetSection(req.getSectionId());
            post.setSection(section);
        }

        if (!isEmpty(req.getSlug())) {
            String candidate = req.getSlug().trim();
            if (!candidate.equals(post.getSlug())) {
                post.setSlug(ensureUniqueSlug(candidate, post.getId()));
            }
        }

        if (req.getContentJson() != null || req.getContent() != null) {
            String normalizedContentJson = resolveContentJson(req.getContentJson(), req.getContent());
            post.setContentJson(normalizedContentJson);
        }

        if (req.getContentMd() != null)
            post.setContentMd(req.getContentMd());
        else if (req.getContent() != null)
            post.setContentMd(req.getContent());
        if (req.getContentHtml() != null)
            post.setContentHtml(req.getContentHtml());
        if (req.getContentText() != null)
            post.setContentText(req.getContentText());

        PostEntity saved = postRepository.save(post);

        insertRevision(saved, actor, "SAVE");
        insertAudit(actor, AuditActionEnum.UPDATE, AuditEntityTypeEnum.POST, saved.getId(), null);

        log.debug("[AdminPost][Update] done postId={}", uuidBinaryMapper.toUuid(saved.getId()));
        return postMapper.toDto(saved);
    }

    // =========================
    // GET BY ID (admin)
    // =========================
    @Override
    public PostDto getById(String postId) {
        log.info("[AdminPost][GetById] postId={}", postId);
        PostEntity post = mustGetPost(postId);
        if (post.getDeletedAt() != null)
            throw new EntityNotFoundException("Post not found");
        return postMapper.toDto(post);
    }

    // =========================
    // SEARCH (admin list)
    // =========================
    @Override
    public Page<PostDto> search(PostStatusEnum status, String sectionId, String q, int page, int size) {
        if (page < 0)
            page = 0;
        if (size <= 0)
            size = 10;

        log.info("[AdminPost][Search] status={}, sectionId={}, q={}, page={}, size={}",
                status, sectionId, q, page, size);

        Sort sort = status == PostStatusEnum.PUBLISHED
                ? Sort.by(Sort.Direction.DESC, "publishedAt")
                : Sort.by(Sort.Direction.DESC, "updatedDate");
        Pageable pageable = PageRequest.of(page, size, sort);

        byte[] sid = null;
        if (!isEmpty(sectionId))
            sid = uuidBinaryMapper.toBytes(sectionId);

        Page<PostEntity> entities = postRepository.adminSearch(status, sid, normalizeQuery(q), pageable);

        List<PostDto> dtos = new ArrayList<>();
        for (PostEntity e : entities.getContent()) {
            dtos.add(postMapper.toDto(e));
        }

        log.debug("[AdminPost][Search] result total={}, returned={}",
                entities.getTotalElements(), entities.getNumberOfElements());

        return new PageImpl<>(dtos, pageable, entities.getTotalElements());
    }

    // =========================
    // PUBLISH / UNPUBLISH
    // =========================
    @Override
    @Transactional
    public PostDto publish(String postId, String actorUserId) {
        log.info("[AdminPost][Publish] postId={}, actorUserId={}", postId, actorUserId);

        UserEntity actor = mustGetUser(actorUserId);
        PostEntity post = mustGetPost(postId);

        if (post.getDeletedAt() != null)
            throw new BadRequestException("Post was deleted");
        if (isEmpty(post.getTitle()))
            throw new BadRequestException("Title is required to publish");
        if (post.getSection() == null)
            throw new BadRequestException("Section is required to publish");

        post.setStatus(PostStatusEnum.PUBLISHED);
        // publishedAt: DB trigger set, hoặc bạn set ở đây nếu bạn không dùng trigger
        // post.setPublishedAt(LocalDateTime.now());

        PostEntity saved = postRepository.save(post);

        insertRevision(saved, actor, "PUBLISH");
        insertAudit(actor, AuditActionEnum.PUBLISH, AuditEntityTypeEnum.POST, saved.getId(), null);

        log.info("[AdminPost][Publish] published postId={}", uuidBinaryMapper.toUuid(saved.getId()));
        return postMapper.toDto(saved);
    }

    @Override
    @Transactional
    public PostDto unpublish(String postId, String actorUserId) {
        log.info("[AdminPost][Unpublish] postId={}, actorUserId={}", postId, actorUserId);

        UserEntity actor = mustGetUser(actorUserId);
        PostEntity post = mustGetPost(postId);

        if (post.getDeletedAt() != null)
            throw new BadRequestException("Post was deleted");

        post.setStatus(PostStatusEnum.DRAFT);
        // publishedAt: trigger sẽ set null
        PostEntity saved = postRepository.save(post);

        insertRevision(saved, actor, "UNPUBLISH");
        insertAudit(actor, AuditActionEnum.UNPUBLISH, AuditEntityTypeEnum.POST, saved.getId(), null);

        log.info("[AdminPost][Unpublish] back to draft postId={}", uuidBinaryMapper.toUuid(saved.getId()));
        return postMapper.toDto(saved);
    }

    // =========================
    // FEATURED + SHOW ON FRONT
    // =========================
    @Override
    @Transactional
    public void setFeatured(PostEntity post) {

        // Rule: chỉ 1 featured
        frontPageItemRepository.clearPinned();

        FrontPageItemEntity item = frontPageItemRepository.findByPost(post)
                .orElseGet(() -> {
                    FrontPageItemEntity f = new FrontPageItemEntity();
                    f.setPost(post);
                    f.setPosition(1);
                    f.setActive(true);
                    return f;
                });

        item.setPinned(true);
        item.setActive(true);
        item.setUpdatedAt(LocalDateTime.now());

        frontPageItemRepository.save(item);
    }

    @Override
    @Transactional
    public PostDto toggleShowOnFront(String postId, boolean showOnFront, String actorUserId) {
        log.info("[AdminPost][ToggleShowOnFront] postId={}, showOnFront={}, actorUserId={}",
                postId, showOnFront, actorUserId);

        UserEntity actor = mustGetUser(actorUserId);
        PostEntity post = mustGetPost(postId);

        if (post.getDeletedAt() != null)
            throw new BadRequestException("Post was deleted");

        post.setShowOnFrontPage(showOnFront);
        PostEntity saved = postRepository.save(post);

        insertAudit(actor, AuditActionEnum.UPDATE, AuditEntityTypeEnum.POST, saved.getId(),
                showOnFront ? "{\"showOnFront\":true}" : "{\"showOnFront\":false}");

        return postMapper.toDto(saved);
    }

    // =========================
    // SOFT DELETE
    // =========================
    @Override
    @Transactional
    public void softDelete(String postId, String actorUserId) {
        log.info("[AdminPost][SoftDelete] postId={}, actorUserId={}", postId, actorUserId);

        UserEntity actor = mustGetUser(actorUserId);
        PostEntity post = mustGetPost(postId);

        if (post.getDeletedAt() != null) {
            log.warn("[AdminPost][SoftDelete] already deleted postId={}", postId);
            return;
        }

        post.setDeletedAt(LocalDateTime.now());
        postRepository.save(post);

        insertAudit(actor, AuditActionEnum.DELETE, AuditEntityTypeEnum.POST, post.getId(), null);
        log.info("[AdminPost][SoftDelete] deleted postId={}", postId);
    }

    // =========================
    // Helpers
    // =========================
    private UserEntity mustGetUser(String userId) {
        log.info("[Get User] userId={}", userId);
        if (isEmpty(userId))
            throw new BadRequestException("actorUserId is required");
        byte[] uid = uuidBinaryMapper.toBytes(userId);
        return userRepository.findById(uid)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    private SectionEntity mustGetSection(String sectionId) {
        log.info("[Get Section] sectionId={}", sectionId);
        byte[] sid = uuidBinaryMapper.toBytes(sectionId);
        return sectionRepository.findById(sid)
                .orElseThrow(() -> new EntityNotFoundException("Section not found: " + sectionId));
    }

    private PostEntity mustGetPost(String postId) {
        log.info("[Get Post] postId={}", postId);
        byte[] pid = uuidBinaryMapper.toBytes(postId);
        return postRepository.findById(pid)
                .orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));
    }

    // Sau nay sẽ sử dụng kafka để handle
    private void insertRevision(PostEntity post, UserEntity editor, String reason) {
        log.info("[Start dump revision]");
        int max = postRevisionRepository.getMaxRevisionNo(post.getId());
        PostRevisionEntity rev = new PostRevisionEntity();
        rev.setId(uuidBinaryMapper.newUuidBytes());
        rev.setPost(post);
        rev.setEditor(editor);
        rev.setRevisionNo(max + 1);
        rev.setReason(reason);

        rev.setTitle(post.getTitle());
        rev.setSubtitle(post.getSubtitle());
        rev.setSlug(post.getSlug());
        rev.setStatus(post.getStatus());

        rev.setContentJson(post.getContentJson());
        rev.setContentMd(post.getContentMd());
        rev.setContentHtml(post.getContentHtml());
        rev.setContentText(post.getContentText());
        rev.setContentVersion(post.getContentVersion());
        rev.setCreatedDate(LocalDateTime.now());
        rev.setUpdatedDate(LocalDateTime.now());

        postRevisionRepository.save(rev);
        log.debug("[AdminPost][Revision] postId={}, revisionNo={}, reason={}",
                uuidBinaryMapper.toUuid(post.getId()), rev.getRevisionNo(), reason);
    }

    private void insertAudit(UserEntity actor,
            AuditActionEnum action,
            AuditEntityTypeEnum entityType,
            byte[] entityId,
            String metaJson) {
        log.info("[AdminPost][Start dump Audit user]");
        AuditLogEntity logEntity = new AuditLogEntity();
        logEntity.setActor(actor);
        logEntity.setAction(action);
        logEntity.setEntityType(entityType);
        logEntity.setEntityId(entityId);
        logEntity.setCreatedDate(LocalDateTime.now());
        // metaJson: tuỳ mapping meta là JSON object hay String
        // logEntity.setMeta(metaJson);
        log.debug("[AdminPost][Audit] actor={}, action={}, entityType={}, entityId={}",
                uuidBinaryMapper.toUuid(actor.getId()), action, entityType, uuidBinaryMapper.toUuid(entityId));
        auditLogRepository.save(logEntity);
    }

    private String ensureUniqueSlug(String baseSlug, byte[] currentPostId) {
        String slug = baseSlug;
        int i = 1;

        while (true) {
            PostEntity conflict = postRepository.findBySlug(slug).orElse(null);
            if (conflict == null)
                return slug;

            // update case: nếu conflict chính là post hiện tại thì ok
            if (currentPostId != null && bytesEquals(conflict.getId(), currentPostId)) {
                return slug;
            }

            i++;
            slug = baseSlug + "-" + i;
        }
    }

    private boolean bytesEquals(byte[] a, byte[] b) {
        if (a == b)
            return true;
        if (a == null || b == null)
            return false;
        if (a.length != b.length)
            return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i])
                return false;
        }
        return true;
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String resolveContentJson(String contentJson, String content) {
        if (!isEmpty(contentJson)) {
            String normalized = contentJson.trim();
            ensureValidJson(normalized, "contentJson");
            return normalized;
        }

        if (!isEmpty(content)) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("raw", content);
            payload.put("type", "text");
            try {
                return OBJECT_MAPPER.writeValueAsString(payload);
            } catch (JsonProcessingException e) {
                throw new BadRequestException("content is invalid");
            }
        }

        throw new BadRequestException("content is required");
    }

    private void ensureValidJson(String json, String fieldName) {
        try {
            OBJECT_MAPPER.readTree(json);
        } catch (Exception e) {
            throw new BadRequestException(fieldName + " must be valid JSON");
        }
    }

    private String normalizeQuery(String q) {
        if (q == null)
            return null;
        String t = q.trim();
        return t.isEmpty() ? null : t;
    }
}
