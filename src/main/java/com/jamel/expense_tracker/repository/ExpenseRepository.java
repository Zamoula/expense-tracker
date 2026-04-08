package com.jamel.expense_tracker.repository;

import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import tools.jackson.databind.ObjectMapper;

import com.jamel.expense_tracker.model.Expense;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    // Paginated query with LastEvaluatedKey
    public PaginatedResult<Expense> findPaginatedByUserId(String userId, 
                                                          String lastEvaluatedKey, 
                                                          int limit) {
        // Build the query request
        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                    Key.builder().partitionValue(userId).build()
                ))
                .limit(limit);
        
        // Add pagination token if provided
        if (lastEvaluatedKey != null && !lastEvaluatedKey.isEmpty()) {
            Map<String, AttributeValue> exclusiveStartKey = decodeLastEvaluatedKey(lastEvaluatedKey);
            requestBuilder.exclusiveStartKey(exclusiveStartKey);
        }
        
        // Execute query
        Page<Expense> page = expenseTable.query(requestBuilder.build()).stream()
                .findFirst()
                .orElse(Page.create(Collections.emptyList()));
        
        // Prepare result
        PaginatedResult<Expense> result = new PaginatedResult<>();
        result.setItems(page.items().stream().collect(Collectors.toList()));
        
        // Encode next page token if exists
        if (page.lastEvaluatedKey() != null && !page.lastEvaluatedKey().isEmpty()) {
            result.setLastEvaluatedKey(encodeLastEvaluatedKey(page.lastEvaluatedKey()));
            result.setHasNext(true);
        } else {
            result.setHasNext(false);
        }
        
        result.setSize(result.getItems().size());
        return result;
    }
    
    // Encode the LastEvaluatedKey to a safe string for client
    private String encodeLastEvaluatedKey(Map<String, AttributeValue> lastEvaluatedKey) {
        try {
            // Convert the map to a serializable format
            Map<String, String> serializableMap = new HashMap<>();
            for (Map.Entry<String, AttributeValue> entry : lastEvaluatedKey.entrySet()) {
                serializableMap.put(entry.getKey(), entry.getValue().s());
            }
            // Serialize and encode to Base64
            String json = new ObjectMapper().writeValueAsString(serializableMap);
            return Base64.getEncoder().encodeToString(json.getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode pagination key", e);
        }
    }
    
    // Decode the pagination token back to LastEvaluatedKey
    private Map<String, AttributeValue> decodeLastEvaluatedKey(String encodedKey) {
        try {
            String json = new String(Base64.getDecoder().decode(encodedKey));
            Map<String, String> decodedMap = new ObjectMapper()
                    .readValue(json, HashMap.class);
            
            Map<String, AttributeValue> lastEvaluatedKey = new HashMap<>();
            for (Map.Entry<String, String> entry : decodedMap.entrySet()) {
                lastEvaluatedKey.put(entry.getKey(), AttributeValue.builder().s(entry.getValue()).build());
            }
            return lastEvaluatedKey;
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode pagination key", e);
        }
    }
    
    // Helper class for paginated results
    public static class PaginatedResult<T> {
        private List<T> items;
        private String lastEvaluatedKey;
        private boolean hasNext;
        private int size;
        
        // Getters and setters
        public List<T> getItems() { return items; }
        public void setItems(List<T> items) { this.items = items; }
        public String getLastEvaluatedKey() { return lastEvaluatedKey; }
        public void setLastEvaluatedKey(String lastEvaluatedKey) { this.lastEvaluatedKey = lastEvaluatedKey; }
        public boolean isHasNext() { return hasNext; }
        public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }
}
