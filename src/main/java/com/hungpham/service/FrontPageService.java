package com.hungpham.service;

import com.hungpham.dtos.FrontPageCompositionDto;
import com.hungpham.dtos.FrontPageItemDto;
import com.hungpham.entity.FrontPageItemEntity;
import com.hungpham.requests.UpdateFrontPageItemRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface FrontPageService {

    FrontPageCompositionDto getComposition();

    FrontPageCompositionDto clearFeatured(String actorUserId, Long expectedVersion);

    FrontPageItemDto setFeatured(String postId, String actorUserId, Long expectedVersion);

    FrontPageItemDto upsertCurated(String postId,
                                   int position,
                                   boolean active,
                                   LocalDateTime startAt,
                                   LocalDateTime endAt,
                                   String note,
                                   String actorUserId,
                                   Long expectedVersion);

    List<FrontPageItemEntity> getActiveItemsForNow();

    List<FrontPageItemDto> getAllItemsForAdmin();

    FrontPageItemDto updateItem(Long id, UpdateFrontPageItemRequest req, String actorUserId, Long expectedVersion);

    void deleteItem(Long id, String actorUserId, Long expectedVersion);

    void reorder(List<Long> orderedIds, String actorUserId, Long expectedVersion);
}
