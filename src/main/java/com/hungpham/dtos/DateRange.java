package com.hungpham.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DateRange {
    private final LocalDateTime from;
    private final LocalDateTime to;

    public DateRange(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;
    }
}
