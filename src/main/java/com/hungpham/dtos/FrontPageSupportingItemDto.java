package com.hungpham.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FrontPageSupportingItemDto {

    private Long id;
    private String postId;
    private Integer position;
    private boolean active;
    private boolean pinned;
    private String startAt;
    private String endAt;
    private String note;
    private PostPreviewDto post;
}
