package com.hungpham.dtos;

import com.hungpham.common.enums.PostMediaRoleEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostMediaDto {

    private String postId;
    private String mediaId;

    private PostMediaRoleEnum mediaRole;
    private Integer position;
}
