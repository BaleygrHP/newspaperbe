package com.hungpham.controller.admin;

import com.hungpham.common.exception.BadRequestException;
import com.hungpham.dtos.FrontPageCompositionDto;
import com.hungpham.dtos.FrontPageItemDto;
import com.hungpham.requests.ReorderFrontPageItemsRequest;
import com.hungpham.requests.UpdateFrontPageItemRequest;
import com.hungpham.requests.UpsertCuratedRequest;
import com.hungpham.service.FrontPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/admin/front-page")
public class FrontPageAdminController {

    @Autowired
    private FrontPageService frontPageService;

    private String actor(@RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return actorUserId;
    }

    private Long expectedVersion(HttpServletRequest request) {
        String ifMatch = request.getHeader("If-Match");
        String fallback = request.getHeader("X-FrontPage-Version");
        String raw = (ifMatch != null && !ifMatch.trim().isEmpty()) ? ifMatch : fallback;
        if (raw == null || raw.trim().isEmpty()) return null;

        String normalized = raw.trim();
        if (normalized.startsWith("W/")) {
            normalized = normalized.substring(2).trim();
        }
        if (normalized.startsWith("\"") && normalized.endsWith("\"") && normalized.length() >= 2) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        if (normalized.isEmpty()) return null;

        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("front page version is invalid");
        }
    }

    @GetMapping("/composition")
    public FrontPageCompositionDto getComposition() {
        return frontPageService.getComposition();
    }

    @PostMapping("/featured")
    public FrontPageItemDto setFeatured(@RequestParam("postId") String postId,
                                        HttpServletRequest request,
                                        @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return frontPageService.setFeatured(postId, actor(actorUserId), expectedVersion(request));
    }

    @DeleteMapping("/featured")
    public FrontPageCompositionDto clearFeatured(HttpServletRequest request,
                                                 @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return frontPageService.clearFeatured(actor(actorUserId), expectedVersion(request));
    }

    @PostMapping("/curated")
    public FrontPageItemDto upsertCurated(@RequestBody UpsertCuratedRequest req,
                                          HttpServletRequest request,
                                          @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return frontPageService.upsertCurated(
                req.getPostId(),
                req.getPosition(),
                req.isActive(),
                req.getStartAt(),
                req.getEndAt(),
                req.getNote(),
                actor(actorUserId),
                expectedVersion(request)
        );
    }

    @GetMapping("/items")
    public List<FrontPageItemDto> listItems() {
        return frontPageService.getAllItemsForAdmin();
    }

    @PatchMapping("/items/{id}")
    public FrontPageItemDto updateItem(@PathVariable Long id,
                                       @RequestBody UpdateFrontPageItemRequest req,
                                       HttpServletRequest request,
                                       @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return frontPageService.updateItem(id, req, actor(actorUserId), expectedVersion(request));
    }

    @PostMapping("/reorder")
    public void reorder(@RequestBody ReorderFrontPageItemsRequest req,
                        HttpServletRequest request,
                        @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        frontPageService.reorder(req.getOrderedIds(), actor(actorUserId), expectedVersion(request));
    }

    @DeleteMapping("/items/{id}")
    public void delete(@PathVariable Long id,
                       HttpServletRequest request,
                       @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        frontPageService.deleteItem(id, actor(actorUserId), expectedVersion(request));
    }
}
