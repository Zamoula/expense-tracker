package com.jamel.expense_tracker.service;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import com.jamel.expense_tracker.dto.PaginatedResponse;
import com.jamel.expense_tracker.dto.PaginationRequest;
import com.jamel.expense_tracker.model.Expense;
import com.jamel.expense_tracker.repository.ExpenseRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpenseService {
    
    private final ExpenseRepository expenseRepository;
    
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }
    
    @CacheEvict(value = {"userExpenses", "totalExpenses", "expensesByCategory", "categoryTotal", "paginatedExpenses"}, allEntries = true)
    public Expense createExpense(String userId, String title, Double amount, 
                                  String category, String description) {
        Expense expense = Expense.builder()
                .userId(userId)
                .expenseId(UUID.randomUUID().toString())
                .title(title)
                .amount(amount)
                .category(category.toUpperCase())
                .description(description)
                .date(LocalDateTime.now())
                .build();
        
        return expenseRepository.save(expense);
    }
    
    @Cacheable(value = "userExpenses", key = "#userId")
    public List<Expense> getUserExpenses(String userId) {
        return expenseRepository.findByUserId(userId);
    }
    
    @Cacheable(value = "expense", key = "#userId + '-' + #expenseId")
    public Expense getExpense(String userId, String expenseId) {
        Expense expense = expenseRepository.findById(userId, expenseId);
        if (expense == null) {
            throw new RuntimeException("Expense not found with ID: " + expenseId);
        }
        return expense;
    }
    
    @Caching(evict = {
        @CacheEvict(value = "expense", key = "#userId + '-' + #expenseId"),
        @CacheEvict(value = {"userExpenses", "totalExpenses", "expensesByCategory", "categoryTotal", "paginatedExpenses"}, allEntries = true)
    })
    public Expense updateExpense(String userId, String expenseId, 
                                  String title, Double amount, 
                                  String category, String description) {
        Expense existing = expenseRepository.findById(userId, expenseId);
        if (existing == null) {
            throw new RuntimeException("Expense not found with ID: " + expenseId);
        }
        
        existing.setTitle(title);
        existing.setAmount(amount);
        existing.setCategory(category.toUpperCase());
        existing.setDescription(description);
        
        return expenseRepository.update(existing);
    }
    
    @Caching(evict = {
        @CacheEvict(value = "expense", key = "#userId + '-' + #expenseId"),
        @CacheEvict(value = {"userExpenses", "totalExpenses", "expensesByCategory", "categoryTotal", "paginatedExpenses"}, allEntries = true)
    })
    public void deleteExpense(String userId, String expenseId) {
        if (!expenseRepository.exists(userId, expenseId)) {
            throw new RuntimeException("Expense not found with ID: " + expenseId);
        }
        expenseRepository.delete(userId, expenseId);
    }
    
    @Cacheable(value = "totalExpenses", key = "#userId")
    public double getTotalExpenses(String userId) {
        return getUserExpenses(userId).stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }
    
    @Cacheable(value = "expensesByCategory", key = "#userId + '-' + #category")
    public List<Expense> getExpensesByCategory(String userId, String category) {
        return getUserExpenses(userId).stream()
                .filter(expense -> expense.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }
    
    @Cacheable(value = "categoryTotal", key = "#userId + '-' + #category")
    public double getCategoryTotal(String userId, String category) {
        return getExpensesByCategory(userId, category).stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    @Cacheable(value = "paginatedExpenses", key = "#userId + '-' + (#paginationRequest.lastEvaluatedKey != null ? #paginationRequest.lastEvaluatedKey : 'firstPage') + '-' + #paginationRequest.limit")
    public PaginatedResponse<Expense> getUserExpensesPaginated(String userId, 
                                                                PaginationRequest paginationRequest) {
        // Validate limit (max 100 items per page)
        int limit = Math.min(paginationRequest.getLimit(), 100);
        
        // Query repository with pagination
        ExpenseRepository.PaginatedResult<Expense> result = 
            expenseRepository.findPaginatedByUserId(userId, 
                paginationRequest.getLastEvaluatedKey(), 
                limit);
        
        // Build response
        PaginatedResponse<Expense> response = new PaginatedResponse<>();
        response.setItems(result.getItems());
        
        PaginatedResponse.PageInfo pageInfo = new PaginatedResponse.PageInfo();
        pageInfo.setSize(result.getSize());
        pageInfo.setLastEvaluatedKey(result.getLastEvaluatedKey());
        pageInfo.setHasNext(result.isHasNext());
        
        // Optional: Get total count (requires additional query)
        // pageInfo.setTotalItems(expenseRepository.countByUserId(userId));
        
        response.setPageInfo(pageInfo);
        return response;
    }
}
