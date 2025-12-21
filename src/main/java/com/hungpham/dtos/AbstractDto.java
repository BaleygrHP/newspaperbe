package com.hungpham.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public abstract class AbstractDto<T> {
    private String id;
    private String createdDate;
    private String updatedDate;
}
