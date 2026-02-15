package com.hungpham.requests.section;

import com.hungpham.common.enums.SectionVisibilityEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSectionRequest {
    private String name;
    private String description;
    private Integer sortOrder;
    private Boolean active;
    private SectionVisibilityEnum visibility;
}
