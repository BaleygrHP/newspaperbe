package com.hungpham.requests.section;


import com.hungpham.common.enums.SectionVisibilityEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSectionRequest {
    private String key;                // unique: editorial/notes/diary...
    private String name;               // display name
    private Integer sortOrder;         // menu order
    private Boolean active;            // default true
    private SectionVisibilityEnum visibility; // PUBLIC/PRIVATE
}
