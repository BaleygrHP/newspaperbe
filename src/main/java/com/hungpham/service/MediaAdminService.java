package com.hungpham.service;

import com.hungpham.common.enums.MediaKindEnum;
import com.hungpham.dtos.MediaBinaryDto;
import com.hungpham.dtos.MediaAssetDto;
import com.hungpham.requests.media.CreateMediaByUrlRequest;
import com.hungpham.requests.media.UpdateMediaRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaAdminService {
    Page<MediaAssetDto> search(MediaKindEnum kind, Boolean active, String category, String q, int page, int size);

    MediaAssetDto getById(String id);

    MediaAssetDto createByUrl(CreateMediaByUrlRequest req, String actorUserId);

    MediaAssetDto createByUpload(MultipartFile file,
                                 MediaKindEnum kind,
                                 String title,
                                 String alt,
                                 String caption,
                                 String location,
                                 String takenAt,
                                 String category,
                                 String actorUserId);

    MediaAssetDto update(String id, UpdateMediaRequest req, String actorUserId);

    void disable(String id, String actorUserId);

    List<String> listCategories();

    MediaBinaryDto loadBinaryForAdmin(String id);
}
