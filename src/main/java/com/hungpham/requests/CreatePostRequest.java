package com.hungpham.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePostRequest {

    // required
    private String title;
    private String sectionId;
    private String contentJson;

    // optional
    private String subtitle;
    private String slug;

    // content cache (optional)
    private String contentMd;
    private String contentHtml;
    private String contentText;

    // frontend compatibility
    private String section; // key
    private String excerpt;
    private String coverImageUrl;
    private String content;
}
