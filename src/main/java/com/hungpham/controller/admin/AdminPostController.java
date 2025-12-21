package com.hungpham.controller.admin;

import com.hungpham.common.enums.PostStatusEnum;
import com.hungpham.dtos.PostDto;
import com.hungpham.requests.CreatePostRequest;
import com.hungpham.requests.UpdatePostRequest;
import com.hungpham.service.PostAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/posts")
public class AdminPostController {

    @Autowired
    private PostAdminService postAdminService;

    // Tạm thời phase 1: actorUserId lấy từ header để pass nhanh
    private String actor(@RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return actorUserId; // phase 2 sẽ lấy từ security context
    }

    @PostMapping
    public PostDto createDraft(@RequestBody CreatePostRequest req,
                               @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return postAdminService.createDraft(req, actor(actorUserId));
    }

    @PutMapping("/{postId}")
    public PostDto update(@PathVariable String postId,
                          @RequestBody UpdatePostRequest req,
                          @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return postAdminService.updatePost(postId, req, actor(actorUserId));
    }

    @GetMapping("/{postId}")
    public PostDto getById(@PathVariable String postId) {
        return postAdminService.getById(postId);
    }

    @GetMapping
    public Page<PostDto> search(
            @RequestParam(value = "status", required = false) PostStatusEnum status,
            @RequestParam(value = "sectionId", required = false) String sectionId,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return postAdminService.search(status, sectionId, q, page, size);
    }

    @PostMapping("/{postId}/publish")
    public PostDto publish(@PathVariable String postId,
                           @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return postAdminService.publish(postId, actor(actorUserId));
    }

    @PostMapping("/{postId}/unpublish")
    public PostDto unpublish(@PathVariable String postId,
                             @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return postAdminService.unpublish(postId, actor(actorUserId));
    }

    @DeleteMapping("/{postId}")
    public void softDelete(@PathVariable String postId,
                           @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        postAdminService.softDelete(postId, actor(actorUserId));
    }
}
