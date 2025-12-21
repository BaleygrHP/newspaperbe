package com.hungpham.service;

import com.hungpham.common.enums.PostStatusEnum;
import com.hungpham.dtos.PostDto;

import com.hungpham.entity.PostEntity;
import com.hungpham.requests.CreatePostRequest;
import com.hungpham.requests.UpdatePostRequest;
import org.springframework.data.domain.Page;

public interface PostAdminService {

    PostDto createDraft(CreatePostRequest req, String actorUserId);

    PostDto updatePost(String postId, UpdatePostRequest req, String actorUserId);

    PostDto getById(String postId);

    Page<PostDto> search(PostStatusEnum status, String sectionId, String q, int page, int size);

    PostDto publish(String postId, String actorUserId);

    PostDto unpublish(String postId, String actorUserId);

    void setFeatured(PostEntity post);

    PostDto toggleShowOnFront(String postId, boolean showOnFront, String actorUserId);

    void softDelete(String postId, String actorUserId);
}
