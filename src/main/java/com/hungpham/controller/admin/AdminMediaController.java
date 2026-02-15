package com.hungpham.controller.admin;

import com.hungpham.common.enums.MediaKindEnum;
import com.hungpham.dtos.MediaAssetDto;
import com.hungpham.dtos.MediaBinaryDto;
import com.hungpham.requests.media.CreateMediaByUrlRequest;
import com.hungpham.requests.media.UpdateMediaRequest;
import com.hungpham.service.MediaAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MediaAssetDto upload(@RequestPart("file") MultipartFile file,
                                @RequestParam(value = "kind", required = false) MediaKindEnum kind,
                                @RequestParam(value = "title", required = false) String title,
                                @RequestParam(value = "alt", required = false) String alt,
                                @RequestParam(value = "caption", required = false) String caption,
                                @RequestParam(value = "location", required = false) String location,
                                @RequestParam(value = "takenAt", required = false) String takenAt,
                                @RequestParam(value = "category", required = false) String category,
                                @RequestHeader(value = "X-Actor-UserId", required = false) String actorUserId) {
        return mediaAdminService.createByUpload(
                file,
                kind,
                title,
                alt,
                caption,
                location,
                takenAt,
                category,
                actor(actorUserId)
        );
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

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable String id) {
        MediaBinaryDto binary = mediaAdminService.loadBinaryForAdmin(id);
        if (binary.isRedirect()) {
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                    .location(URI.create(binary.getRedirectUrl()))
                    .build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(safeMediaType(binary.getMimeType()));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, buildContentDisposition("attachment", binary.getFileName()));
        headers.setCacheControl("no-store");
        headers.set("X-Content-Type-Options", "nosniff");
        headers.setContentLength(binary.getContentLength());
        if (binary.getETag() != null) headers.setETag(binary.getETag());

        return new ResponseEntity<>(binary.getContent(), headers, HttpStatus.OK);
    }

    private MediaType safeMediaType(String mimeType) {
        try {
            return MediaType.parseMediaType(mimeType == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : mimeType);
        } catch (Exception ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private String buildContentDisposition(String mode, String fileName) {
        String safeName = sanitizeFileName(fileName);
        String asciiName = safeName.replaceAll("[^\\x20-\\x7E]", "_");
        String encoded;
        try {
            encoded = URLEncoder.encode(safeName, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (Exception ex) {
            encoded = asciiName;
        }
        return mode + "; filename=\"" + asciiName + "\"; filename*=UTF-8''" + encoded;
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) return "download.bin";
        String normalized = fileName.replace('\\', '/');
        int idx = normalized.lastIndexOf('/');
        String base = idx >= 0 ? normalized.substring(idx + 1) : normalized;
        base = base.replace("\"", "_").replace("\r", "_").replace("\n", "_").trim();
        return base.isEmpty() ? "download.bin" : base;
    }
}
