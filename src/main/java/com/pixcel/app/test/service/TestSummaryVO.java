package com.pixcel.app.test.service;

import lombok.Data;

@Data
public class TestSummaryVO {
    private Integer totalCount;
    private Integer expectedCount;
    private Integer progressCount;
    private Integer endCount;
}
