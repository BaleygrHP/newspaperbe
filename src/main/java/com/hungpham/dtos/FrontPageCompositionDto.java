package com.hungpham.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FrontPageCompositionDto {

    private String status;
    private Long version;
    private String updatedAt;
    private PostPreviewDto featured;
    private List<FrontPageSupportingItemDto> items;
}
