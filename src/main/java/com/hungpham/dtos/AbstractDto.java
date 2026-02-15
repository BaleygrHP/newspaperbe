package com.hungpham.dtos;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public abstract class AbstractDto<T> {
    private String id;
    private String createdDate;
    private String updatedDate;
}
