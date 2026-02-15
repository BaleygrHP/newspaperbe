package com.hungpham.controller.publicapi;

import com.hungpham.common.enums.MediaKindEnum;
import com.hungpham.dtos.MediaBinaryDto;
import com.hungpham.dtos.PublicMediaDto;
import com.hungpham.service.MediaPublicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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

    @GetMapping("/{id}/content")
    public ResponseEntity<byte[]> content(@PathVariable String id,
                                          @RequestHeader(value = "Range", required = false) String range,
                                          @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        return renderContent(id, range, ifNoneMatch, false);
    }

    @RequestMapping(value = "/{id}/content", method = RequestMethod.HEAD)
    public ResponseEntity<byte[]> headContent(@PathVariable String id,
                                              @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        return renderContent(id, null, ifNoneMatch, true);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable String id) {
        MediaBinaryDto binary = mediaPublicService.loadActiveBinary(id);
        if (binary.isRedirect()) {
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                    .location(URI.create(binary.getRedirectUrl()))
                    .build();
        }

        HttpHeaders headers = baseHeaders(binary, "attachment");
        headers.setCacheControl("no-store");
        return new ResponseEntity<>(binary.getContent(), headers, HttpStatus.OK);
    }

    private ResponseEntity<byte[]> renderContent(String id, String range, String ifNoneMatch, boolean headOnly) {
        MediaBinaryDto binary = mediaPublicService.loadActiveBinary(id);
        if (binary.isRedirect()) {
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                    .location(URI.create(binary.getRedirectUrl()))
                    .build();
        }

        HttpHeaders headers = baseHeaders(binary, "inline");
        headers.setCacheControl(binary.getCacheControl() == null ? "public, max-age=60" : binary.getCacheControl());
        if (binary.isAcceptsRanges()) headers.set("Accept-Ranges", "bytes");

        if (binary.getETag() != null && binary.getETag().equals(ifNoneMatch)) {
            return new ResponseEntity<>(null, headers, HttpStatus.NOT_MODIFIED);
        }

        long total = binary.getContentLength();
        if (!headOnly && binary.isAcceptsRanges() && range != null && range.startsWith("bytes=")) {
            ByteRange parsed = parseRange(range, total);
            if (parsed == null) {
                headers.set("Content-Range", "bytes */" + total);
                return new ResponseEntity<>(null, headers, HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
            }

            int start = (int) parsed.getStart();
            int end = (int) parsed.getEnd();
            byte[] part = Arrays.copyOfRange(binary.getContent(), start, end + 1);
            headers.setContentLength(part.length);
            headers.set("Content-Range", "bytes " + start + "-" + end + "/" + total);
            return new ResponseEntity<>(part, headers, HttpStatus.PARTIAL_CONTENT);
        }

        headers.setContentLength(total);
        return new ResponseEntity<>(headOnly ? null : binary.getContent(), headers, HttpStatus.OK);
    }

    private HttpHeaders baseHeaders(MediaBinaryDto binary, String dispositionMode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(safeMediaType(binary.getMimeType()));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, buildContentDisposition(dispositionMode, binary.getFileName()));
        headers.set("X-Content-Type-Options", "nosniff");
        if (binary.getETag() != null) headers.setETag(binary.getETag());
        return headers;
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
        if (fileName == null || fileName.trim().isEmpty()) return "media.bin";
        String normalized = fileName.replace('\\', '/');
        int idx = normalized.lastIndexOf('/');
        String base = idx >= 0 ? normalized.substring(idx + 1) : normalized;
        base = base.replace("\"", "_").replace("\r", "_").replace("\n", "_").trim();
        return base.isEmpty() ? "media.bin" : base;
    }

    private ByteRange parseRange(String rangeHeader, long totalLength) {
        try {
            String value = rangeHeader.substring("bytes=".length()).trim();
            if (value.contains(",")) return null;
            int dash = value.indexOf('-');
            if (dash < 0) return null;

            String startPart = value.substring(0, dash).trim();
            String endPart = value.substring(dash + 1).trim();

            long start;
            long end;

            if (startPart.isEmpty()) {
                long suffixLength = Long.parseLong(endPart);
                if (suffixLength <= 0) return null;
                start = Math.max(0, totalLength - suffixLength);
                end = totalLength - 1;
            } else {
                start = Long.parseLong(startPart);
                end = endPart.isEmpty() ? totalLength - 1 : Long.parseLong(endPart);
            }

            if (start < 0 || end < start || start >= totalLength) return null;
            end = Math.min(end, totalLength - 1);
            return new ByteRange(start, end);
        } catch (Exception ex) {
            return null;
        }
    }

    private static class ByteRange {
        private final long start;
        private final long end;

        private ByteRange(long start, long end) {
            this.start = start;
            this.end = end;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }
    }
}
