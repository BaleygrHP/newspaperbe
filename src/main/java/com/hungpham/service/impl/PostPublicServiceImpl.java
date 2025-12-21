package com.hungpham.service.impl;

import com.hungpham.common.constant.FrontPageConstants;
import com.hungpham.common.constant.SectionKeys;
import com.hungpham.common.enums.PostStatusEnum;
import com.hungpham.common.exception.BadRequestException;
import com.hungpham.common.exception.EntityNotFoundException;
import com.hungpham.dtos.DateRange;
import com.hungpham.dtos.FrontPageDto;
import com.hungpham.dtos.PostDto;
import com.hungpham.entity.FrontPageItemEntity;
import com.hungpham.entity.PostEntity;
import com.hungpham.mappers.PostMapper;
import com.hungpham.repository.FrontPageItemRepository;
import com.hungpham.repository.PostRepository;
import com.hungpham.service.PostPublicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PostPublicServiceImpl implements PostPublicService {

    private static final Logger log = LoggerFactory.getLogger(PostPublicServiceImpl.class);


    @Autowired
    private PostRepository postRepository;

    @Autowired
    private FrontPageItemRepository frontPageItemRepository;

    @Autowired
    private PostMapper postMapper;

    //  FRONT PAGE
    @Override
    public FrontPageDto getFrontPage() {
        LocalDateTime now = LocalDateTime.now();
        log.info("[FrontPage] Start loading front page");
        // 1) Featured (1 bài)
        PostDto featured = frontPageItemRepository.findActiveFeatured(now)
                .stream()
                .findFirst()
                .map(item -> postMapper.toDto(item.getPost()))
                .orElse(null);
        log.debug("[FrontPage] Featured found: {}", featured);

        // 2) Latest (6 bài)
        Pageable latestPage = PageRequest.of(0, 6, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<PostEntity> latestEntities =
                postRepository.findByStatusAndDeletedAtIsNull(PostStatusEnum.PUBLISHED, latestPage);
        log.debug("[FrontPage] Latest posts count: {}", latestEntities.getNumberOfElements());

        List<PostDto> latest = mapList(latestEntities.getContent());

        // 3) Curated (front_page_items) – optional
        List<PostDto> curated = new ArrayList<>();

        List<FrontPageItemEntity> curatedEntities = frontPageItemRepository.findActiveFrontPageItems(now);
        for (FrontPageItemEntity f : curatedEntities) {
            PostEntity p = f.getPost();
            if (p != null && p.getDeletedAt() == null && p.getStatus() == PostStatusEnum.PUBLISHED) {
                curated.add(postMapper.toDto(p));
            }
        }
        log.debug("[FrontPage] Curated items count: {}", curatedEntities.size());

        // 4) Editorial block (2–3)
        List<PostDto> editorialBlock = getTopBySection(SectionKeys.EDITORIAL, FrontPageConstants.EDITORIAL_LIMIT);
        log.debug("[FrontPage] editorial items count: {}", editorialBlock.size());
        // 5) Diary block (1–2)
        List<PostDto> diaryBlock = getTopBySection(SectionKeys.DIARY, FrontPageConstants.DIARY_LIMIT);
        log.debug("[FrontPage] diaryBlock items count: {}", diaryBlock.size());
        // 6) Notes block (2–3)
        List<PostDto> notesBlock = getTopBySection(SectionKeys.NOTES, FrontPageConstants.NOTES_LIMIT);
        log.debug("[FrontPage] notesBlock items count: {}", notesBlock.size());

        FrontPageDto dto = new FrontPageDto();
        dto.setFeatured(featured);
        dto.setLatest(latest);
        dto.setCurated(curated);
        dto.setEditorialBlock(editorialBlock);
        dto.setDiaryBlock(diaryBlock);
        dto.setNotesBlock(notesBlock);
        log.info("[FrontPage] Done loading front page");
        return dto;
    }

    private List<PostDto> getTopBySection(String sectionKey, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<PostEntity> entities = postRepository.findBySection_KeyAndStatusAndDeletedAtIsNull(
                sectionKey,
                PostStatusEnum.PUBLISHED,
                pageable
        );
        return mapList(entities.getContent());
    }

    //  SECTION LIST
    @Override
    public Page<PostDto> getSectionPosts(String sectionKey, int page, int size) {

        if (sectionKey == null || sectionKey.trim().isEmpty()) {
            log.warn("[SectionPosts] Missing sectionKey");
            throw new BadRequestException("sectionKey is required");
        }
        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        log.info("[SectionPosts]Get section posts sectionKey={}, page={}, size={}", sectionKey, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<PostEntity> entities = postRepository.findBySection_KeyAndStatusAndDeletedAtIsNull(
                sectionKey.trim(),
                PostStatusEnum.PUBLISHED,
                pageable
        );
        log.debug("[SectionPosts] Found {} posts (total={})",
                entities.getNumberOfElements(),
                entities.getTotalElements());
        List<PostDto> postDtos = mapList(entities.getContent());
        return new PageImpl<>(postDtos, pageable, entities.getTotalElements());
    }

    //  ARTICLE (BY SLUG)
    @Override
    public PostDto getPublishedPostBySlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            log.warn("[GetPostBySlug] Empty slug");
            throw new BadRequestException("slug is required");
        }
        log.info("[GetPostBySlug] Get published post by slug {}", slug);

        PostEntity entity = postRepository.findBySlugAndStatusAndDeletedAtIsNull(slug.trim(), PostStatusEnum.PUBLISHED)
                .orElseThrow(() -> {
                    log.warn("[GetPostBySlug] Post not found slug={}", slug);
                    return new EntityNotFoundException("Post not found: " + slug);
                });

        log.debug("[GetPostBySlug] Post id={}", entity.getId());
        return postMapper.toDto(entity);
    }

    //  RELATED POSTS
    @Override
    public List<PostDto> getRelatedPosts(String sectionKey, String slug, int limit) {
        if (sectionKey == null || sectionKey.trim().isEmpty()) {
            throw new BadRequestException("sectionKey is required");
        }
        if (slug == null || slug.trim().isEmpty()) {
            throw new BadRequestException("slug is required");
        }
        if (limit <= 0) limit = 5;

        log.info("[RelatedPosts] Get related posts sectionKey={}, slug={}, limit={}", sectionKey, slug, limit);

        // 1) Lấy current post để có postId (để exclude)
        PostEntity current = postRepository.findBySlugAndStatusAndDeletedAtIsNull(
                        slug.trim(),
                        PostStatusEnum.PUBLISHED
                )
                .orElseThrow(() -> {
                    log.warn("[RelatedPosts] Current post not found slug={}", slug);
                    return new EntityNotFoundException("Post not found: " + slug);
                });

        // 2) Query related theo sectionKey, exclude postId
        Pageable pageable = PageRequest.of(0, limit);
        List<PostEntity> related = postRepository.findRelatedPosts(
                sectionKey.trim(),
                current.getId(),
                PostStatusEnum.PUBLISHED,
                pageable
        );

        // 3) Map
        List<PostDto> dtos = new ArrayList<>();
        for (PostEntity p : related) {
            dtos.add(postMapper.toDto(p));
        }
        log.debug("[RelatedPosts] Found {} related posts", related.size());
        return dtos;
    }


    //  ARCHIVE
    @Override
    public Page<PostDto> getArchive(Integer year, Integer month,
                                    String sectionKey, int page, int size) {

        if (year == null || year < 1970) {
            throw new BadRequestException("year is required");
        }
        log.info("[Archive] year={}, month={}, sectionKey={}, page={}, size={}",
                year, month, sectionKey, page, size);
        DateRange range = resolveArchiveRange(year, month);
        log.debug("[Archive] Range from={} to={}", range.getFrom(), range.getTo());

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "publishedAt"));

        Page<PostEntity> entities;
        if (sectionKey == null || sectionKey.trim().isEmpty()) {
            entities = postRepository.findArchivePublished(
                    range.getFrom(),
                    range.getTo(),
                    pageable
            );
        } else {
            entities = postRepository.findArchivePublishedBySection(
                    sectionKey.trim(),
                    range.getFrom(),
                    range.getTo(),
                    pageable
            );
        }
        log.debug("[Archive] Found {} posts", entities.getTotalElements());
        return new PageImpl<>(
                mapList(entities.getContent()),
                pageable,
                entities.getTotalElements()
        );
    }


    private List<PostDto> mapList(List<PostEntity> entities) {
        List<PostDto> dtos = new ArrayList<>();
        if (entities == null) return dtos;
        for (PostEntity e : entities) {
            dtos.add(postMapper.toDto(e));
        }
        return dtos;
    }

    private DateRange resolveArchiveRange(int year, Integer month) {
        if (month == null) {
            return new DateRange(
                    LocalDateTime.of(year, 1, 1, 0, 0),
                    LocalDateTime.of(year + 1, 1, 1, 0, 0)
            );
        }

        if (month < 1 || month > 12) {
            throw new BadRequestException("month must be between 1 and 12");
        }

        LocalDateTime from = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime to = (month == 12)
                ? LocalDateTime.of(year + 1, 1, 1, 0, 0)
                : LocalDateTime.of(year, month + 1, 1, 0, 0);

        return new DateRange(from, to);
    }
}
