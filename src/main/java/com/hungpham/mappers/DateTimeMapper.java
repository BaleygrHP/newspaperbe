package com.hungpham.mappers;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeMapper {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Named("ldtToString")
    public String ldtToString(LocalDateTime value) {
        return value == null ? null : value.format(ISO);
    }

    @Named("stringToLdt")
    public LocalDateTime stringToLdt(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return null;
        return LocalDateTime.parse(trimmed, ISO);
    }
}
