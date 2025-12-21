package com.hungpham.controller.publicapi;

import com.hungpham.dtos.FrontPageDto;
import com.hungpham.dtos.PostDto;
import com.hungpham.service.PostPublicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/posts")
public class PublicPostController {

    @Autowired
    private PostPublicService postPublicService;

    // Trang chủ
    @GetMapping("/front-page")
    public FrontPageDto getFrontPage() {
        return postPublicService.getFrontPage();
    }

    // Đọc bài theo slug
    @GetMapping("/{slug}")
    public PostDto getBySlug(@PathVariable String slug) {
        return postPublicService.getPublishedPostBySlug(slug);
    }

    // Danh sách bài theo section
    @GetMapping
    public Page<PostDto> getSectionPosts(
            @RequestParam("sectionKey") String sectionKey,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return postPublicService.getSectionPosts(sectionKey, page, size);
    }

    // Archive theo year/month + (optional) sectionKey
    @GetMapping("/archive")
    public Page<PostDto> getArchive(
            @RequestParam("year") Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "sectionKey", required = false) String sectionKey,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return postPublicService.getArchive(year, month, sectionKey, page, size);
    }

    // Related posts
    @GetMapping("/{slug}/related")
    public List<PostDto> getRelated(
            @PathVariable String slug,
            @RequestParam("sectionKey") String sectionKey,
            @RequestParam(value = "limit", defaultValue = "6") int limit
    ) {
        return postPublicService.getRelatedPosts(sectionKey, slug, limit);
    }
}
