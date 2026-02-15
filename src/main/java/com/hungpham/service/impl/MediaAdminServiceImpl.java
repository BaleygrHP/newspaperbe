package com.hungpham.service.impl;

import com.hungpham.common.enums.*;
import com.hungpham.common.exception.BadRequestException;
import com.hungpham.common.exception.EntityNotFoundException;
import com.hungpham.dtos.MediaAssetDto;
import com.hungpham.dtos.MediaBinaryDto;
import com.hungpham.entity.AuditLogEntity;
import com.hungpham.entity.MediaAssetEntity;
import com.hungpham.entity.UserEntity;
import com.hungpham.mappers.MediaAssetMapper;
import com.hungpham.mappers.UuidBinaryMapper;
import com.hungpham.repository.AuditLogRepository;
import com.hungpham.repository.MediaAssetRepository;
import com.hungpham.repository.UserRepository;
import com.hungpham.requests.media.CreateMediaByUrlRequest;
import com.hungpham.requests.media.UpdateMediaRequest;
import com.hungpham.service.MediaAdminService;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class MediaAdminServiceImpl implements MediaAdminService {

    @Autowired private MediaAssetRepository mediaAssetRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private MediaAssetMapper mediaAssetMapper;
    @Autowired private UuidBinaryMapper uuidBinaryMapper;

    @Value("${app.media.max-bytes:26214400}")
    private long maxBytes;

    @Value("${app.media.local-storage.root:./storage/media}")
    private String localStorageRoot;

    @Value("${app.media.allowed-mime-types:image/jpeg,image/png,image/webp,image/gif,image/avif,image/svg+xml,video/mp4,video/webm,application/pdf}")
    private String allowedMimeTypes;

    @Value("${app.media.disallowed-mime-types:application/x-msdownload,text/html}")
    private String disallowedMimeTypes;

    @Value("${app.media.url-fetch.connect-timeout-ms:5000}")
    private int urlConnectTimeoutMs;

    @Value("${app.media.url-fetch.read-timeout-ms:15000}")
    private int urlReadTimeoutMs;

    @Value("${app.media.url-fetch.max-bytes:26214400}")
    private long urlMaxBytes;

    @Value("${app.media.url-fetch.max-redirects:3}")
    private int urlMaxRedirects;

    @Value("${app.media.url-fetch.block-private-network:true}")
    private boolean blockPrivateNetwork;

    private final Tika tika = new Tika();

    private static final String DEFAULT_MIME = "application/octet-stream";
    private static final String CACHE_CONTROL_CONTENT = "public, max-age=31536000, immutable";
    private static final String PUBLIC_CONTENT_PATH = "/api/public/media/%s/content";

    private static final Map<String, String> MIME_TO_EXT = new HashMap<String, String>();
    private static final Map<String, String> EXT_TO_MIME = new HashMap<String, String>();

    static {
        registerMime("image/jpeg", "jpg", "jpeg");
        registerMime("image/png", "png");
        registerMime("image/webp", "webp");
        registerMime("image/gif", "gif");
        registerMime("image/avif", "avif");
        registerMime("image/svg+xml", "svg");
        registerMime("image/bmp", "bmp");
        registerMime("image/tiff", "tif", "tiff");
        registerMime("video/mp4", "mp4");
        registerMime("video/webm", "webm");
        registerMime("video/quicktime", "mov");
        registerMime("application/pdf", "pdf");
    }

    @Override
    public Page<MediaAssetDto> search(MediaKindEnum kind, Boolean active, String category, String q, int page, int size) {
        String nq = normalizeQuery(q);
        String ncat = normalizeQuery(category);

        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "createdDate")
        );

        Page<MediaAssetEntity> p = mediaAssetRepository.adminSearch(kind, active, ncat, nq, pageable);
        return p.map(mediaAssetMapper::toDto);
    }

    @Override
    public MediaAssetDto getById(String id) {
        MediaAssetEntity e = mustGetMedia(id);
        return mediaAssetMapper.toDto(e);
    }

    @Transactional
    @Override
    public MediaAssetDto createByUrl(CreateMediaByUrlRequest req, String actorUserId) {
        UserEntity actor = mustGetUser(actorUserId);

        if (req == null) throw new BadRequestException("request is required");
        if (isEmpty(req.getUrl())) throw new BadRequestException("url is required");

        UrlFetchResult fetched = fetchFromUrl(req.getUrl());
        String detectedMime = detectMimeType(fetched.getBytes(), fetched.getContentType(), req.getMimeType(), req.getUrl());
        validateMimeTypeAllowed(detectedMime);

        MediaKindEnum resolvedKind = resolveKind(req.getKind(), detectedMime);
        String fileHash = sha256Hex(fetched.getBytes());

        Optional<MediaAssetEntity> existingOpt = mediaAssetRepository.findByFileHash(fileHash);
        if (existingOpt.isPresent()) {
            return handleDedupe(existingOpt.get(), actor);
        }

        String extension = extensionFromMime(detectedMime);
        String storageKey = buildStorageKey(extension);
        writeLocal(storageKey, fetched.getBytes());

        MediaAssetEntity e = new MediaAssetEntity();
        e.setId(uuidBinaryMapper.newUuidBytes());
        e.setOwner(actor);
        e.setKind(resolvedKind);
        e.setStorage(MediaStorageEnum.LOCAL);
        e.setUrl(String.format(PUBLIC_CONTENT_PATH, uuidBinaryMapper.toUuid(e.getId())));
        e.setStorageKey(storageKey);
        e.setMimeType(detectedMime);
        e.setByteSize(fetched.getBytes().length);
        MediaDimensions dimensions = extractImageDimensions(detectedMime, fetched.getBytes());
        e.setWidth(dimensions.getWidth());
        e.setHeight(dimensions.getHeight());
        e.setAlt(req.getAlt());
        e.setTitle(req.getTitle());
        e.setOriginalFileName(sanitizeFileName(extractFileNameFromUrl(req.getUrl())));
        e.setCaption(req.getCaption());
        e.setLocation(req.getLocation());
        e.setCategory(req.getCategory());
        e.setTakenAt(parseTakenAt(req.getTakenAt()));
        e.setFileHash(fileHash);
        e.setActive(true);

        try {
            mediaAssetRepository.save(e);
        } catch (DataIntegrityViolationException ex) {
            cleanupLocalQuietly(storageKey);
            Optional<MediaAssetEntity> raced = mediaAssetRepository.findByFileHash(fileHash);
            if (raced.isPresent()) return handleDedupe(raced.get(), actor);
            throw ex;
        }

        insertAudit(actor, AuditActionEnum.CREATE, AuditEntityTypeEnum.MEDIA, e.getId());
        return mediaAssetMapper.toDto(e);
    }

    @Transactional
    @Override
    public MediaAssetDto createByUpload(MultipartFile file,
                                        MediaKindEnum kind,
                                        String title,
                                        String alt,
                                        String caption,
                                        String location,
                                        String takenAt,
                                        String category,
                                        String actorUserId) {
        UserEntity actor = mustGetUser(actorUserId);

        if (file == null || file.isEmpty()) throw new BadRequestException("file is required");
        if (file.getSize() > maxBytes) throw new BadRequestException("file exceeds max allowed size");

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new BadRequestException("failed to read upload file");
        }

        String detectedMime = detectMimeType(bytes, file.getContentType(), null, file.getOriginalFilename());
        validateMimeTypeAllowed(detectedMime);
        MediaKindEnum resolvedKind = resolveKind(kind, detectedMime);
        String fileHash = sha256Hex(bytes);

        Optional<MediaAssetEntity> existingOpt = mediaAssetRepository.findByFileHash(fileHash);
        if (existingOpt.isPresent()) {
            return handleDedupe(existingOpt.get(), actor);
        }

        String extension = extensionFromMime(detectedMime);
        String storageKey = buildStorageKey(extension);
        writeLocal(storageKey, bytes);

        MediaAssetEntity e = new MediaAssetEntity();
        e.setId(uuidBinaryMapper.newUuidBytes());
        e.setOwner(actor);
        e.setKind(resolvedKind);
        e.setStorage(MediaStorageEnum.LOCAL);
        e.setUrl(String.format(PUBLIC_CONTENT_PATH, uuidBinaryMapper.toUuid(e.getId())));
        e.setStorageKey(storageKey);
        e.setMimeType(detectedMime);
        e.setByteSize(bytes.length);
        MediaDimensions dimensions = extractImageDimensions(detectedMime, bytes);
        e.setWidth(dimensions.getWidth());
        e.setHeight(dimensions.getHeight());
        e.setTitle(title);
        e.setAlt(alt);
        e.setCaption(caption);
        e.setLocation(location);
        e.setCategory(category);
        e.setTakenAt(parseTakenAt(takenAt));
        e.setOriginalFileName(sanitizeFileName(file.getOriginalFilename()));
        e.setFileHash(fileHash);
        e.setActive(true);

        try {
            mediaAssetRepository.save(e);
        } catch (DataIntegrityViolationException ex) {
            cleanupLocalQuietly(storageKey);
            Optional<MediaAssetEntity> raced = mediaAssetRepository.findByFileHash(fileHash);
            if (raced.isPresent()) return handleDedupe(raced.get(), actor);
            throw ex;
        }

        insertAudit(actor, AuditActionEnum.CREATE, AuditEntityTypeEnum.MEDIA, e.getId());
        return mediaAssetMapper.toDto(e);
    }

    @Transactional
    @Override
    public MediaAssetDto update(String id, UpdateMediaRequest req, String actorUserId) {
        UserEntity actor = mustGetUser(actorUserId);
        MediaAssetEntity e = mustGetMedia(id);

        if (req == null) throw new BadRequestException("request is required");

        if (req.getAlt() != null) e.setAlt(req.getAlt());
        if (req.getTitle() != null) e.setTitle(req.getTitle());
        if (req.getCaption() != null) e.setCaption(req.getCaption());
        if (req.getLocation() != null) e.setLocation(req.getLocation());
        if (req.getCategory() != null) e.setCategory(req.getCategory());

        if (req.getTakenAt() != null) {
            if (isEmpty(req.getTakenAt())) {
                e.setTakenAt(null);
            } else {
                e.setTakenAt(parseTakenAt(req.getTakenAt()));
            }
        }

        if (req.getActive() != null) e.setActive(req.getActive());

        mediaAssetRepository.save(e);
        insertAudit(actor, AuditActionEnum.UPDATE, AuditEntityTypeEnum.MEDIA, e.getId());

        return mediaAssetMapper.toDto(e);
    }

    @Transactional
    @Override
    public void disable(String id, String actorUserId) {
        UserEntity actor = mustGetUser(actorUserId);
        MediaAssetEntity e = mustGetMedia(id);

        e.setActive(false);
        mediaAssetRepository.save(e);

        insertAudit(actor, AuditActionEnum.DELETE, AuditEntityTypeEnum.MEDIA, e.getId());
    }

    @Override
    public List<String> listCategories() {
        return mediaAssetRepository.listDistinctCategories();
    }

    @Override
    public MediaBinaryDto loadBinaryForAdmin(String id) {
        return toBinaryDto(mustGetMedia(id));
    }

    // =========================
    // Binary / Storage helpers
    // =========================

    private MediaBinaryDto toBinaryDto(MediaAssetEntity entity) {
        if (entity.getStorage() != MediaStorageEnum.LOCAL) {
            if (isEmpty(entity.getUrl())) {
                throw new EntityNotFoundException("Media source URL is empty");
            }
            MediaBinaryDto redirected = new MediaBinaryDto();
            redirected.setRedirect(true);
            redirected.setRedirectUrl(entity.getUrl());
            redirected.setMimeType(normalizeMime(entity.getMimeType()));
            redirected.setFileName(resolveFileName(entity));
            redirected.setETag(buildEtag(entity));
            redirected.setAcceptsRanges(false);
            redirected.setNosniff(true);
            redirected.setCacheControl("no-store");
            return redirected;
        }

        String normalizedStorageKey = normalizeStorageKey(entity.getStorageKey());
        if (isEmpty(normalizedStorageKey)) {
            throw new EntityNotFoundException("Missing storage key for LOCAL media");
        }

        byte[] bytes = readLocal(normalizedStorageKey);
        MediaBinaryDto dto = new MediaBinaryDto();
        dto.setRedirect(false);
        dto.setContent(bytes);
        dto.setContentLength(bytes.length);
        dto.setMimeType(nonEmptyOrDefault(normalizeMime(entity.getMimeType()), DEFAULT_MIME));
        dto.setFileName(resolveFileName(entity));
        dto.setETag(buildEtag(entity));
        dto.setCacheControl(CACHE_CONTROL_CONTENT);
        dto.setNosniff(true);
        dto.setAcceptsRanges(true);
        return dto;
    }

    private void writeLocal(String storageKey, byte[] bytes) {
        Path path = resolveLocalPath(storageKey);
        try {
            Path parent = path.getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.write(path, bytes, StandardOpenOption.CREATE_NEW);
        } catch (IOException ex) {
            throw new BadRequestException("failed to store media file");
        }
    }

    private byte[] readLocal(String storageKey) {
        Path path = resolveLocalPath(storageKey);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new EntityNotFoundException("Media file not found on storage");
        }
        try {
            return Files.readAllBytes(path);
        } catch (IOException ex) {
            throw new BadRequestException("failed to read media file");
        }
    }

    private void cleanupLocalQuietly(String storageKey) {
        try {
            Path path = resolveLocalPath(storageKey);
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
        }
    }

    private Path resolveLocalPath(String storageKey) {
        Path root = Paths.get(localStorageRoot).toAbsolutePath().normalize();
        String normalizedKey = normalizeStorageKey(storageKey);
        Path resolved = root.resolve(normalizedKey).normalize();
        if (!resolved.startsWith(root)) {
            throw new BadRequestException("invalid storage key");
        }
        return resolved;
    }

    private String normalizeStorageKey(String storageKey) {
        if (isEmpty(storageKey)) return null;
        String normalized = storageKey.trim().replace('\\', '/');
        if (normalized.startsWith("/") || normalized.contains("..") || normalized.contains(":")) {
            throw new BadRequestException("invalid storage key");
        }
        while (normalized.contains("//")) {
            normalized = normalized.replace("//", "/");
        }
        return normalized;
    }

    private String buildStorageKey(String extension) {
        LocalDate date = LocalDate.now();
        String safeExt = extension == null ? "bin" : extension;
        return String.format(
                "%04d/%02d/%02d/%s.%s",
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth(),
                UUID.randomUUID().toString(),
                safeExt
        );
    }

    // =========================
    // Upload / URL ingestion helpers
    // =========================

    private UrlFetchResult fetchFromUrl(String rawUrl) {
        URL current = parseHttpUrl(rawUrl);
        int redirects = 0;

        while (true) {
            List<InetAddress> pinned = resolveAndValidateHost(current.getHost());
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) current.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(urlConnectTimeoutMs);
                conn.setReadTimeout(urlReadTimeoutMs);
                conn.setInstanceFollowRedirects(false);

                int status = conn.getResponseCode();
                ensureDnsStable(current.getHost(), pinned);

                if (isRedirect(status)) {
                    if (redirects >= Math.max(0, urlMaxRedirects)) {
                        throw new BadRequestException("too many redirects");
                    }
                    String location = conn.getHeaderField("Location");
                    if (isEmpty(location)) throw new BadRequestException("redirect location is missing");
                    current = resolveRedirect(current, location);
                    redirects++;
                    continue;
                }

                if (status < 200 || status >= 300) {
                    throw new BadRequestException("failed to fetch url, status: " + status);
                }

                long contentLength = conn.getContentLengthLong();
                long maxAllowed = Math.min(maxBytes, urlMaxBytes);
                if (contentLength > 0 && contentLength > maxAllowed) {
                    throw new BadRequestException("remote file exceeds max allowed size");
                }

                byte[] bytes = readLimitedBytes(conn.getInputStream(), maxAllowed);
                UrlFetchResult result = new UrlFetchResult();
                result.setBytes(bytes);
                result.setContentType(conn.getContentType());
                result.setFinalUrl(current.toString());
                return result;
            } catch (IOException ex) {
                throw new BadRequestException("failed to fetch media url");
            } finally {
                if (conn != null) conn.disconnect();
            }
        }
    }

    private URL parseHttpUrl(String rawUrl) {
        if (isEmpty(rawUrl)) throw new BadRequestException("url is required");
        try {
            URI uri = new URI(rawUrl.trim());
            String scheme = uri.getScheme();
            if (scheme == null) throw new BadRequestException("url scheme is required");
            String normalizedScheme = scheme.toLowerCase(Locale.ROOT);
            if (!"http".equals(normalizedScheme) && !"https".equals(normalizedScheme)) {
                throw new BadRequestException("only http/https urls are allowed");
            }
            if (isEmpty(uri.getHost())) throw new BadRequestException("url host is required");
            return uri.toURL();
        } catch (URISyntaxException | MalformedURLException ex) {
            throw new BadRequestException("invalid url");
        }
    }

    private URL resolveRedirect(URL current, String location) {
        try {
            URL next = new URL(current, location);
            String scheme = next.getProtocol();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new BadRequestException("redirect url must be http/https");
            }
            if (isEmpty(next.getHost())) throw new BadRequestException("redirect host is required");
            return next;
        } catch (MalformedURLException ex) {
            throw new BadRequestException("invalid redirect url");
        }
    }

    private boolean isRedirect(int status) {
        return status == 301 || status == 302 || status == 303 || status == 307 || status == 308;
    }

    private List<InetAddress> resolveAndValidateHost(String host) {
        if (!blockPrivateNetwork) return Collections.emptyList();
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            if (addresses == null || addresses.length == 0) {
                throw new BadRequestException("unable to resolve host");
            }
            List<InetAddress> out = new ArrayList<InetAddress>(addresses.length);
            for (InetAddress addr : addresses) {
                if (isPrivateAddress(addr)) {
                    throw new BadRequestException("blocked private or local address");
                }
                out.add(addr);
            }
            return out;
        } catch (UnknownHostException ex) {
            throw new BadRequestException("unable to resolve host");
        }
    }

    private void ensureDnsStable(String host, List<InetAddress> pinned) {
        if (!blockPrivateNetwork || pinned == null || pinned.isEmpty()) return;

        List<InetAddress> now = resolveAndValidateHost(host);
        Set<String> pinnedSet = new HashSet<String>();
        for (InetAddress addr : pinned) {
            pinnedSet.add(addr.getHostAddress());
        }

        boolean overlap = false;
        for (InetAddress addr : now) {
            if (pinnedSet.contains(addr.getHostAddress())) {
                overlap = true;
                break;
            }
        }

        if (!overlap) {
            throw new BadRequestException("host resolution changed during fetch");
        }
    }

    private boolean isPrivateAddress(InetAddress address) {
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {
            return true;
        }

        byte[] raw = address.getAddress();
        if (raw == null) return true;

        if (raw.length == 4) {
            int b0 = raw[0] & 0xFF;
            int b1 = raw[1] & 0xFF;
            if (b0 == 169 && b1 == 254) return true;
            if (b0 == 100 && b1 >= 64 && b1 <= 127) return true;
            if (b0 == 198 && (b1 == 18 || b1 == 19)) return true;
        } else if (raw.length == 16) {
            int first = raw[0] & 0xFF;
            int second = raw[1] & 0xFF;
            if ((first & 0xFE) == 0xFC) return true;
            if (first == 0xFE && (second & 0xC0) == 0x80) return true;
        }

        return false;
    }

    private byte[] readLimitedBytes(InputStream in, long maxAllowed) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        long total = 0L;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while ((read = in.read(buffer)) != -1) {
            total += read;
            if (total > maxAllowed) {
                throw new BadRequestException("file exceeds max allowed size");
            }
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private String detectMimeType(byte[] bytes, String headerHint, String requestHint, String nameHint) {
        String detectedFromBytes = normalizeMime(tika.detect(bytes, nonEmptyOrDefault(nameHint, "file.bin")));
        if (isGoodMime(detectedFromBytes)) return detectedFromBytes;

        String fromHeader = normalizeMime(headerHint);
        if (isGoodMime(fromHeader)) return fromHeader;

        String fromRequest = normalizeMime(requestHint);
        if (isGoodMime(fromRequest)) return fromRequest;

        String ext = extensionFromName(nameHint);
        if (ext != null && EXT_TO_MIME.containsKey(ext)) return EXT_TO_MIME.get(ext);
        return DEFAULT_MIME;
    }

    private MediaDimensions extractImageDimensions(String mimeType, byte[] bytes) {
        MediaDimensions dimensions = new MediaDimensions();
        String normalized = normalizeMime(mimeType);
        if (normalized == null || !normalized.startsWith("image/")) return dimensions;

        try {
            java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(bytes));
            if (image != null) {
                dimensions.setWidth(image.getWidth());
                dimensions.setHeight(image.getHeight());
            }
        } catch (IOException ignored) {
        }
        return dimensions;
    }

    private void validateMimeTypeAllowed(String mimeType) {
        String normalized = normalizeMime(mimeType);
        if (isEmpty(normalized)) throw new BadRequestException("unable to determine mimeType");

        Set<String> disallowed = parseMimeSet(disallowedMimeTypes);
        if (matchesAnyMimePattern(normalized, disallowed)) {
            throw new BadRequestException("mimeType is not allowed: " + normalized);
        }

        Set<String> allowed = parseMimeSet(allowedMimeTypes);
        if (!allowed.isEmpty() && !matchesAnyMimePattern(normalized, allowed)) {
            throw new BadRequestException("mimeType is not allowed: " + normalized);
        }
    }

    private Set<String> parseMimeSet(String csv) {
        Set<String> set = new HashSet<String>();
        if (isEmpty(csv)) return set;
        String[] parts = csv.split(",");
        for (String p : parts) {
            String normalized = normalizeMime(p);
            if (!isEmpty(normalized)) set.add(normalized);
        }
        return set;
    }

    private boolean matchesAnyMimePattern(String mimeType, Set<String> patterns) {
        if (patterns == null || patterns.isEmpty()) return false;
        for (String pattern : patterns) {
            if (matchesMimePattern(mimeType, pattern)) return true;
        }
        return false;
    }

    private boolean matchesMimePattern(String mimeType, String pattern) {
        if (isEmpty(mimeType) || isEmpty(pattern)) return false;
        if (pattern.endsWith("/*")) {
            String prefix = pattern.substring(0, pattern.length() - 1);
            return mimeType.startsWith(prefix);
        }
        return mimeType.equals(pattern);
    }

    private MediaKindEnum resolveKind(MediaKindEnum requested, String detectedMimeType) {
        MediaKindEnum inferred = inferKindFromMime(detectedMimeType);
        if (requested == null) return inferred;
        if (requested != inferred) {
            throw new BadRequestException("kind does not match detected mimeType");
        }
        return requested;
    }

    private MediaKindEnum inferKindFromMime(String mimeType) {
        String normalized = normalizeMime(mimeType);
        if (normalized != null && normalized.startsWith("image/")) return MediaKindEnum.IMAGE;
        if (normalized != null && normalized.startsWith("video/")) return MediaKindEnum.VIDEO;
        return MediaKindEnum.FILE;
    }

    private String normalizeMime(String mimeType) {
        if (mimeType == null) return null;
        String normalized = mimeType.trim().toLowerCase(Locale.ROOT);
        int semicolon = normalized.indexOf(';');
        if (semicolon >= 0) normalized = normalized.substring(0, semicolon).trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean isGoodMime(String mimeType) {
        String normalized = normalizeMime(mimeType);
        if (normalized == null) return false;
        return !DEFAULT_MIME.equals(normalized);
    }

    private String extensionFromMime(String mimeType) {
        String normalized = normalizeMime(mimeType);
        if (normalized == null) return "bin";
        String ext = MIME_TO_EXT.get(normalized);
        return ext == null ? "bin" : ext;
    }

    private static void registerMime(String mimeType, String... extensions) {
        if (extensions == null || extensions.length == 0) return;
        MIME_TO_EXT.put(mimeType, extensions[0]);
        for (String ext : extensions) {
            EXT_TO_MIME.put(ext.toLowerCase(Locale.ROOT), mimeType);
        }
    }

    private String extensionFromName(String name) {
        if (isEmpty(name)) return null;
        String sanitized = sanitizeFileName(name);
        if (isEmpty(sanitized)) return null;
        int dot = sanitized.lastIndexOf('.');
        if (dot < 0 || dot == sanitized.length() - 1) return null;
        return sanitized.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String sha256Hex(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private MediaAssetDto handleDedupe(MediaAssetEntity existing, UserEntity actor) {
        if (!existing.isActive()) {
            existing.setActive(true);
            mediaAssetRepository.save(existing);
            insertAudit(actor, AuditActionEnum.UPDATE, AuditEntityTypeEnum.MEDIA, existing.getId());
        }
        return mediaAssetMapper.toDto(existing);
    }

    // =========================
    // Domain helpers
    // =========================
    private UserEntity mustGetUser(String userId) {
        if (isEmpty(userId)) throw new BadRequestException("actorUserId is required");
        byte[] uid = uuidBinaryMapper.toBytes(userId);
        return userRepository.findById(uid)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    private MediaAssetEntity mustGetMedia(String id) {
        if (isEmpty(id)) throw new BadRequestException("id is required");
        byte[] mid = uuidBinaryMapper.toBytes(id);
        return mediaAssetRepository.findById(mid)
                .orElseThrow(() -> new EntityNotFoundException("Media not found: " + id));
    }

    private void insertAudit(UserEntity actor,
                             AuditActionEnum action,
                             AuditEntityTypeEnum entityType,
                             byte[] entityId) {
        AuditLogEntity logEntity = new AuditLogEntity();
        logEntity.setActor(actor);
        logEntity.setAction(action);
        logEntity.setEntityType(entityType);
        logEntity.setEntityId(entityId);
        logEntity.setCreatedDate(LocalDateTime.now());
        auditLogRepository.save(logEntity);
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String nonEmptyOrDefault(String value, String defaultValue) {
        return isEmpty(value) ? defaultValue : value;
    }

    private String normalizeQuery(String q) {
        if (q == null) return null;
        String t = q.trim();
        return t.isEmpty() ? null : t;
    }

    private String sanitizeFileName(String input) {
        if (isEmpty(input)) return null;
        String normalized = input.replace('\\', '/');
        int idx = normalized.lastIndexOf('/');
        String name = idx >= 0 ? normalized.substring(idx + 1) : normalized;
        name = name.replace("\"", "_")
                .replace("\r", "_")
                .replace("\n", "_")
                .trim();
        if (name.isEmpty()) return null;
        if (name.length() > 255) return name.substring(0, 255);
        return name;
    }

    private String extractFileNameFromUrl(String rawUrl) {
        try {
            URL url = new URL(rawUrl);
            return sanitizeFileName(url.getPath());
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    private LocalDateTime parseTakenAt(String takenAt) {
        if (isEmpty(takenAt)) return null;
        try {
            return LocalDate.parse(takenAt.trim()).atStartOfDay();
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("takenAt must be yyyy-MM-dd");
        }
    }

    private String buildEtag(MediaAssetEntity entity) {
        if (!isEmpty(entity.getFileHash())) {
            return "\"sha256:" + entity.getFileHash() + "\"";
        }
        String id = uuidBinaryMapper.toUuid(entity.getId());
        String updated = entity.getUpdatedDate() == null
                ? ""
                : String.valueOf(entity.getUpdatedDate().toEpochSecond(ZoneOffset.UTC));
        return "W/\"" + id + "-" + updated + "\"";
    }

    private String resolveFileName(MediaAssetEntity entity) {
        String original = sanitizeFileName(entity.getOriginalFileName());
        if (!isEmpty(original)) return original;
        String ext = extensionFromMime(entity.getMimeType());
        String id = uuidBinaryMapper.toUuid(entity.getId());
        return "media-" + id + "." + ext;
    }

    private static class UrlFetchResult {
        private byte[] bytes;
        private String contentType;
        private String finalUrl;

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getFinalUrl() {
            return finalUrl;
        }

        public void setFinalUrl(String finalUrl) {
            this.finalUrl = finalUrl;
        }
    }

    private static class MediaDimensions {
        private Integer width;
        private Integer height;

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }
    }
}
