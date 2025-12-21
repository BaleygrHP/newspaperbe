package com.hungpham.service;

import com.hungpham.entity.FrontPageItemEntity;
import com.hungpham.requests.UpdateFrontPageItemRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface FrontPageService {

    FrontPageItemEntity setFeatured(String postId, String actorUserId);

    FrontPageItemEntity upsertCurated(String postId,
                                      int position,
                                      boolean active,
                                      LocalDateTime startAt,
                                      LocalDateTime endAt,
                                      String note,
                                      String actorUserId);

    List<FrontPageItemEntity> getActiveItemsForNow();
    // ===== Admin functions (thiếu) =====
    List<FrontPageItemEntity> getAllItemsForAdmin();

    FrontPageItemEntity updateItem(Long id, UpdateFrontPageItemRequest req, String actorUserId);

    void deleteItem(Long id, String actorUserId);

    void reorder(List<Long> orderedIds, String actorUserId);

}
