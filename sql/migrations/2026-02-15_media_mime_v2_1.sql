USE newspaper;

-- Add storage_key if missing
SET @has_storage_key := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'media_assets'
    AND COLUMN_NAME = 'storage_key'
);
SET @sql_storage_key := IF(
  @has_storage_key = 0,
  'ALTER TABLE media_assets ADD COLUMN storage_key VARCHAR(512) NULL AFTER url',
  'SELECT 1'
);
PREPARE stmt_storage_key FROM @sql_storage_key;
EXECUTE stmt_storage_key;
DEALLOCATE PREPARE stmt_storage_key;

-- Add original_file_name if missing
SET @has_original_file_name := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'media_assets'
    AND COLUMN_NAME = 'original_file_name'
);
SET @sql_original_file_name := IF(
  @has_original_file_name = 0,
  'ALTER TABLE media_assets ADD COLUMN original_file_name VARCHAR(255) NULL AFTER title',
  'SELECT 1'
);
PREPARE stmt_original_file_name FROM @sql_original_file_name;
EXECUTE stmt_original_file_name;
DEALLOCATE PREPARE stmt_original_file_name;

-- Add (storage, storage_key) index if missing
SET @has_idx_storage_key := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'media_assets'
    AND INDEX_NAME = 'idx_media_storage_key'
);
SET @sql_idx_storage_key := IF(
  @has_idx_storage_key = 0,
  'CREATE INDEX idx_media_storage_key ON media_assets(storage, storage_key)',
  'SELECT 1'
);
PREPARE stmt_idx_storage_key FROM @sql_idx_storage_key;
EXECUTE stmt_idx_storage_key;
DEALLOCATE PREPARE stmt_idx_storage_key;

-- Ensure file_hash unique key exists for dedupe race-safety
SET @has_uq_media_hash := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'media_assets'
    AND INDEX_NAME = 'uq_media_hash'
    AND NON_UNIQUE = 0
);
SET @sql_uq_media_hash := IF(
  @has_uq_media_hash = 0,
  'ALTER TABLE media_assets ADD UNIQUE KEY uq_media_hash (file_hash)',
  'SELECT 1'
);
PREPARE stmt_uq_media_hash FROM @sql_uq_media_hash;
EXECUTE stmt_uq_media_hash;
DEALLOCATE PREPARE stmt_uq_media_hash;

-- Best-effort backfill original_file_name from URL path if absent
UPDATE media_assets
SET original_file_name = SUBSTRING_INDEX(
  SUBSTRING_INDEX(
    SUBSTRING_INDEX(url, '?', 1),
    '#',
    1
  ),
  '/',
  -1
)
WHERE (original_file_name IS NULL OR TRIM(original_file_name) = '')
  AND url IS NOT NULL
  AND TRIM(url) <> ''
  AND SUBSTRING_INDEX(
    SUBSTRING_INDEX(
      SUBSTRING_INDEX(url, '?', 1),
      '#',
      1
    ),
    '/',
    -1
  ) <> '';

-- Best-effort metadata normalization for legacy rows
UPDATE media_assets
SET mime_type = 'application/octet-stream'
WHERE mime_type IS NULL OR TRIM(mime_type) = '';

UPDATE media_assets
SET byte_size = 0
WHERE byte_size IS NULL;
