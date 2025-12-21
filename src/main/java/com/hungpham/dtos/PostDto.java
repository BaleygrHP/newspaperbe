package com.hungpham.dtos;

import com.hungpham.common.enums.PostStatusEnum;
import com.hungpham.common.enums.SectionEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostDto extends AbstractDto<PostDto> {

    private String title;
    private String subtitle;
    private String excerpt;
    private String contentMd;

    private String slug;
    private String coverImageUrl;
    private String thumbnailUrl;

    private SectionEnum section;
    private PostStatusEnum status;

    private boolean featured;
    private boolean showOnFront;
    private Integer frontRank;

    private String publishedAt;
}
