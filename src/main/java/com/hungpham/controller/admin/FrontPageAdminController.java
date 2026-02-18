package com.hungpham.controller.admin;

import com.hungpham.common.exception.BadRequestException;
import com.hungpham.config.security.AuthContext;
import com.hungpham.dtos.FrontPageCompositionDto;
import com.hungpham.dtos.FrontPageItemDto;
import com.hungpham.requests.ReorderFrontPageItemsRequest;
import com.hungpham.requests.UpdateFrontPageItemRequest;
import com.hungpham.requests.UpsertCuratedRequest;
import com.hungpham.service.FrontPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/admin/front-page")
@PreAuthorize("hasAnyRole('ADMIN','EDITOR')")
public class FrontPageAdminController {

    @Autowired
    private FrontPageService frontPageService;

    @Autowired
    private AuthContext authContext;

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
                                        HttpServletRequest request) {
        return frontPageService.setFeatured(postId, authContext.requireUserId(), expectedVersion(request));
    }

    @DeleteMapping("/featured")
    public FrontPageCompositionDto clearFeatured(HttpServletRequest request) {
        return frontPageService.clearFeatured(authContext.requireUserId(), expectedVersion(request));
    }

    @PostMapping("/curated")
    public FrontPageItemDto upsertCurated(@RequestBody UpsertCuratedRequest req,
                                          HttpServletRequest request) {
        return frontPageService.upsertCurated(
                req.getPostId(),
                req.getPosition(),
                req.isActive(),
                req.getStartAt(),
                req.getEndAt(),
                req.getNote(),
                authContext.requireUserId(),
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
                                       HttpServletRequest request) {
        return frontPageService.updateItem(id, req, authContext.requireUserId(), expectedVersion(request));
    }

    @PostMapping("/reorder")
    public void reorder(@RequestBody ReorderFrontPageItemsRequest req,
                        HttpServletRequest request) {
        frontPageService.reorder(req.getOrderedIds(), authContext.requireUserId(), expectedVersion(request));
    }

    @DeleteMapping("/items/{id}")
    public void delete(@PathVariable Long id,
                       HttpServletRequest request) {
        frontPageService.deleteItem(id, authContext.requireUserId(), expectedVersion(request));
    }
}
