package com.hungpham.service;

import com.hungpham.common.enums.MediaKindEnum;
import com.hungpham.dtos.MediaAssetDto;
import com.hungpham.requests.media.CreateMediaByUrlRequest;
import com.hungpham.requests.media.UpdateMediaRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MediaAdminService {
    Page<MediaAssetDto> search(MediaKindEnum kind, Boolean active, String category, String q, int page, int size);

    MediaAssetDto getById(String id);

    MediaAssetDto createByUrl(CreateMediaByUrlRequest req, String actorUserId);

    MediaAssetDto update(String id, UpdateMediaRequest req, String actorUserId);

    void disable(String id, String actorUserId);

    List<String> listCategories();
}
