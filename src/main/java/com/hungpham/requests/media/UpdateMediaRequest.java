package com.hungpham.requests.media;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMediaRequest {
    private String alt;
    private String title;

    private String caption;
    private String location;
    private String takenAt;  // yyyy-MM-dd
    private String category;

    private Boolean active;
}
