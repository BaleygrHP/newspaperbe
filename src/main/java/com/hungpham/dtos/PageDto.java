package com.hungpham.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PageDto<T> {
    private List<T> items;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;

}
