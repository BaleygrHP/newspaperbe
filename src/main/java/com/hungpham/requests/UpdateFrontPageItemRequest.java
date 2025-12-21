package com.hungpham.requests;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateFrontPageItemRequest {
    private Boolean active;
    private Boolean pinned;
    private Integer position;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String note;
}
