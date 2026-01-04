package com.hungpham.controller.admin;

import com.hungpham.common.enums.MediaKindEnum;
import com.hungpham.dtos.MediaAssetDto;
import com.hungpham.requests.media.CreateMediaByUrlRequest;
import com.hungpham.requests.media.UpdateMediaRequest;
import com.hungpham.service.MediaAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/media")
public class AdminMediaController {

    @Autowired
    private MediaAdminService mediaAdminService;

    private String actor(@RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return actorUserId;
    }

    @GetMapping
    public Page<MediaAssetDto> search(
            @RequestParam(value = "kind", required = false) MediaKindEnum kind,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "24") int size
    ) {
        return mediaAdminService.search(kind, active, category, q, page, size);
    }

    @GetMapping("/{id}")
    public MediaAssetDto getById(@PathVariable String id) {
        return mediaAdminService.getById(id);
    }

    @PostMapping("/url")
    public MediaAssetDto createByUrl(@RequestBody CreateMediaByUrlRequest req,
                                     @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return mediaAdminService.createByUrl(req, actor(actorUserId));
    }

    @PatchMapping("/{id}")
    public MediaAssetDto update(@PathVariable String id,
                                @RequestBody UpdateMediaRequest req,
                                @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return mediaAdminService.update(id, req, actor(actorUserId));
    }

    @DeleteMapping("/{id}")
    public void disable(@PathVariable String id,
                        @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        mediaAdminService.disable(id, actor(actorUserId));
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return mediaAdminService.listCategories();
    }
}
