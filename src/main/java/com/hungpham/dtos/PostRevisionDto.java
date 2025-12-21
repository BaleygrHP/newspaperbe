package com.hungpham.dtos;

import com.hungpham.common.enums.PostStatusEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRevisionDto extends AbstractDto<PostRevisionDto>{
    private String postId;
    private String editorId;
    private Integer revisionNo;
    private String reason;
    private String title;
    private String subtitle;
    private String slug;
    private PostStatusEnum status;
    private String contentJson;
    private String contentMd;
    private String contentHtml;
    private String contentText;
    private Integer contentVersion;
}
