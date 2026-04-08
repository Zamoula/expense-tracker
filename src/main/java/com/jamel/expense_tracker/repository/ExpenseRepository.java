package com.jamel.expense_tracker.repository;

import org.springframework.stereotype.Repository;

import com.jamel.expense_tracker.model.Expense;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ExpenseRepository {
    
    private final DynamoDbTable<Expense> expenseTable;
    
    public ExpenseRepository(DynamoDbClient dynamoDbClient) {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
        
        this.expenseTable = enhancedClient.table("expenses", 
                TableSchema.fromBean(Expense.class));
    }
    
    public Expense save(Expense expense) {
        expenseTable.putItem(expense);
        return expense;
    }
    
    public Expense findById(String userId, String expenseId) {
        Key key = Key.builder()
                .partitionValue(userId)
                .sortValue(expenseId)
                .build();
        
        return expenseTable.getItem(key);
    }
    
    public List<Expense> findByUserId(String userId) {
        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(userId).build());
        
        return expenseTable.query(queryConditional)
                .items()
                .stream()
                .collect(Collectors.toList());
    }
    
    public void delete(String userId, String expenseId) {
        Key key = Key.builder()
                .partitionValue(userId)
                .sortValue(expenseId)
                .build();
        
        expenseTable.deleteItem(key);
    }
    
    public Expense update(Expense expense) {
        // DynamoDB putItem overwrites existing item
        return save(expense);
    }
    
    public boolean exists(String userId, String expenseId) {
        return findById(userId, expenseId) != null;
    }
}
