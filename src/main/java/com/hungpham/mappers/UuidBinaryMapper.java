package com.hungpham.mappers;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.UUID;

@Component
public class UuidBinaryMapper {

    // UUID String → BINARY(16)
    @Named("toBytes")
    public byte[] toBytes(String uuid) {
        if (uuid == null) return null;
        String trimmed = uuid.trim();
        if (trimmed.isEmpty()) return null;

        UUID u = UUID.fromString(trimmed);
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(u.getMostSignificantBits());
        bb.putLong(u.getLeastSignificantBits());
        return bb.array();
    }
    // BINARY(16) → UUID String
    @Named("toUuid")
    public String toUuid(byte[] bin) {
        if (bin == null || bin.length != 16) return null;

        ByteBuffer bb = ByteBuffer.wrap(bin);
        long high = bb.getLong();
        long low = bb.getLong();
        return new UUID(high, low).toString();
    }

    /** dùng khi tạo entity mới */
    public byte[] newUuidBytes() {
        UUID u = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(u.getMostSignificantBits());
        bb.putLong(u.getLeastSignificantBits());
        return bb.array();
    }
}
