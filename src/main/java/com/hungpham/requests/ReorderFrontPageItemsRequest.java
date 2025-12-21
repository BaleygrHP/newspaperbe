package com.hungpham.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReorderFrontPageItemsRequest {
    // danh sách id theo đúng thứ tự mới (top -> bottom)
    private List<Long> orderedIds;
}
