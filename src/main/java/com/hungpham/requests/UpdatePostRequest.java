package com.hungpham.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePostRequest {

    // optional
    private String title;
    private String subtitle;
    private String sectionId;
    private String slug;

    // content
    private String contentJson;
    private String contentMd;
    private String contentHtml;
    private String contentText;
}
