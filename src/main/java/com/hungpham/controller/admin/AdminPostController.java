package com.hungpham.controller.admin;

import com.hungpham.common.enums.PostStatusEnum;
import com.hungpham.config.security.AuthContext;
import com.hungpham.dtos.PostDto;
import com.hungpham.requests.CreatePostRequest;
import com.hungpham.requests.UpdatePostRequest;
import com.hungpham.service.PostAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/posts")
@PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
public class AdminPostController {

    @Autowired
    private PostAdminService postAdminService;

    @Autowired
    private AuthContext authContext;

    @PostMapping
    public PostDto createDraft(@RequestBody CreatePostRequest req) {
        return postAdminService.createDraft(req, authContext.requireUserId());
    }

    @PutMapping("/{postId}")
    public PostDto update(@PathVariable String postId, @RequestBody UpdatePostRequest req) {
        return postAdminService.updatePost(postId, req, authContext.requireUserId());
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
    public PostDto publish(@PathVariable String postId) {
        return postAdminService.publish(postId, authContext.requireUserId());
    }

    @PostMapping("/{postId}/unpublish")
    public PostDto unpublish(@PathVariable String postId) {
        return postAdminService.unpublish(postId, authContext.requireUserId());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{postId}")
    public void softDelete(@PathVariable String postId) {
        postAdminService.softDelete(postId, authContext.requireUserId());
    }
}
