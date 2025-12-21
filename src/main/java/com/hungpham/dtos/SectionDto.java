package com.hungpham.dtos;

import com.hungpham.common.enums.SectionVisibilityEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SectionDto extends AbstractDto<SectionDto>{
    private String key;
    private String name;
    private String description;
    private Integer sortOrder;
    private boolean active;
    private SectionVisibilityEnum visibility;
    private String icon;

}
