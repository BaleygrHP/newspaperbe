package com.hungpham.controller.publicapi;

import com.hungpham.common.enums.MediaKindEnum;
import com.hungpham.dtos.PublicMediaDto;
import com.hungpham.service.MediaPublicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/media")
public class PublicMediaController {

    @Autowired
    private MediaPublicService mediaPublicService;

    @GetMapping
    public Page<PublicMediaDto> getGallery(
            @RequestParam(value = "kind", required = false) MediaKindEnum kind,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "24") int size
    ) {
        return mediaPublicService.getGallery(kind, page, size);
    }

    @GetMapping("/{id}")
    public PublicMediaDto getById(@PathVariable String id) {
        return mediaPublicService.getActiveById(id);
    }
}
