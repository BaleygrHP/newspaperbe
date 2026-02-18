package com.hungpham.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostPreviewDto {
    private String id;
    private String title;
    private String slug;
    private SectionDto section;
}
