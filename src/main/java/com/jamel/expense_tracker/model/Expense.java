package com.jamel.expense_tracker.model;

import java.time.LocalDateTime;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Expense {
    private String userId;
    private String expenseId;
    private String title;
    private Double amount;
    private String category;
    private String description;
    private LocalDateTime date;

    @DynamoDbPartitionKey
    public String getUserId() {
        return userId;
    }

    @DynamoDbSortKey
    public String getExpenseId() {
        return expenseId;
    }
}
