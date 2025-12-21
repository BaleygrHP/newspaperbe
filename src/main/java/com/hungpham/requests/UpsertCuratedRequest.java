package com.hungpham.requests;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpsertCuratedRequest {
    private String postId;
    private int position;
    private boolean active;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String note;
}
