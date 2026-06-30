package com.pixcel.app.sourcerepository.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class sourcerepositoryPageVO<T> {
private List<T> content;
    
    @Builder.Default
    private int currentPage = 1;
    
    private int totalPages;
    private int totalElements;
    
    @Builder.Default
    private int pageSize = 20;

    /**
     * 페이지 객체 생성 (totalPages 자동 계산)
     */
    public sourcerepositoryPageVO(List<T> content, int currentPage, int totalElements, int pageSize) {
        this.content = content;
        this.currentPage = currentPage;
        this.totalElements = totalElements;
        this.pageSize = pageSize;
        this.totalPages = (totalElements + pageSize - 1) / pageSize;
    }

    /**
     * 다음 페이지 여부
     */
    public boolean hasNextPage() {
        return currentPage < totalPages;
    }

    /**
     * 이전 페이지 여부
     */
    public boolean hasPreviousPage() {
        return currentPage > 1;
    }
}
