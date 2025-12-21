package com.hungpham.entity;

import com.hungpham.common.enums.SectionVisibilityEnum;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "sections")
public class SectionEntity extends BaseUuidEntity {

    @Column(name = "`key`", nullable = false, length = 64, unique = true)
    private String key;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 20)
    private SectionVisibilityEnum visibility;

    @Column(name = "icon", length = 64)
    private String icon;
}
