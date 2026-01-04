package com.hungpham.dtos;

import com.hungpham.common.enums.MediaKindEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublicMediaDto extends AbstractDto<PublicMediaDto> {
    private MediaKindEnum kind;
    private String url;
    private Integer width;
    private Integer height;
    private String alt;
    private String title;
    private boolean active;
    private String caption;
    private String location;
    private String takenAt; // yyyy-MM-dd
    private String category;
}
