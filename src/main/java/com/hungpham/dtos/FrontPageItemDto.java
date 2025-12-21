package com.hungpham.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FrontPageItemDto extends AbstractDto<FrontPageItemDto>{

    private String postId;
    private Integer position;
    private boolean active;
    private boolean pinned;
    private String startAt;
    private String endAt;
    private String note;
    private PostPreviewDto post;
}
