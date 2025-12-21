package com.hungpham.service;

import com.hungpham.dtos.FrontPageDto;
import com.hungpham.dtos.PostDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PostPublicService {
    FrontPageDto getFrontPage();

    Page<PostDto> getSectionPosts(String sectionKey, int page, int size);

    PostDto getPublishedPostBySlug(String slug);

    List<PostDto> getRelatedPosts(String sectionKey, String slug, int limit);

    Page<PostDto> getArchive(Integer year, Integer month, String sectionKey, int page, int size);
}
