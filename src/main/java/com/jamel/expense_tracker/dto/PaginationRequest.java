package com.jamel.expense_tracker.dto;

import lombok.Data;

@Data
public class PaginationRequest {
    private int limit = 10;  // Items per page (default 10)
    private String lastEvaluatedKey;  // Cursor from previous response
}
