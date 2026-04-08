package com.jamel.expense_tracker.dto;

import java.util.List;

import lombok.Data;

@Data
public class PaginatedResponse<T> {
    private List<T> items;
    private PageInfo pageInfo;

    @Data
    public static class PageInfo {
        private int size;
        private String lastEvaluatedKey;  // Encoded cursor for next page
        private boolean hasNext;
        //private int totalItems;  // Optional: requires additional query
    }
}
