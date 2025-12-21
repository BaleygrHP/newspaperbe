package com.hungpham.controller.admin;

import com.hungpham.dtos.FrontPageItemDto;
import com.hungpham.requests.ReorderFrontPageItemsRequest;
import com.hungpham.requests.UpdateFrontPageItemRequest;
import com.hungpham.requests.UpsertCuratedRequest;
import com.hungpham.service.FrontPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/front-page")
public class FrontPageAdminController {

    @Autowired
    private FrontPageService frontPageService;

    private String actor(@RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return actorUserId;
    }

    // Set featured: pinned=true (chỉ 1)
    @PostMapping("/featured")
    public FrontPageItemDto setFeatured(@RequestParam("postId") String postId,
                                        @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return frontPageService.setFeatured(postId, actor(actorUserId));
    }

    // Upsert curated: pinned=false + position + active + time window
    @PostMapping("/curated")
    public FrontPageItemDto upsertCurated(@RequestBody UpsertCuratedRequest req,
                                          @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return frontPageService.upsertCurated(
                req.getPostId(),
                req.getPosition(),
                req.isActive(),
                req.getStartAt(),
                req.getEndAt(),
                req.getNote(),
                actor(actorUserId)
        );
    }

    // ===== Admin load items =====
    @GetMapping("/items")
    public List<FrontPageItemDto> listItems() {
        return frontPageService.getAllItemsForAdmin();
    }


    @PatchMapping("/items/{id}")
    public FrontPageItemDto updateItem(@PathVariable Long id,
                                       @RequestBody UpdateFrontPageItemRequest req,
                                       @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return frontPageService.updateItem(id, req, actor(actorUserId));
    }

    // ===== Reorder =====
    @PostMapping("/reorder")
    public void reorder(@RequestBody ReorderFrontPageItemsRequest req,
                        @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        frontPageService.reorder(req.getOrderedIds(), actor(actorUserId));
    }

    // ===== Delete item =====
    @DeleteMapping("/items/{id}")
    public void delete(@PathVariable Long id,
                       @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        frontPageService.deleteItem(id, actor(actorUserId));
    }
}
