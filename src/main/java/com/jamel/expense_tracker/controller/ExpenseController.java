package com.jamel.expense_tracker.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jamel.expense_tracker.model.Expense;
import com.jamel.expense_tracker.service.ExpenseService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {
    
    private final ExpenseService expenseService;
    
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }
    
    // Create expense
    @PostMapping("/{userId}")
    public ResponseEntity<Expense> createExpense(
            @PathVariable String userId,
            @Valid @RequestBody ExpenseRequest request) {
        
        Expense expense = expenseService.createExpense(
                userId,
                request.getTitle(),
                request.getAmount(),
                request.getCategory(),
                request.getDescription()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(expense);
    }
    
    // Get all expenses for user
    @GetMapping("/{userId}")
    public ResponseEntity<List<Expense>> getUserExpenses(@PathVariable String userId) {
        return ResponseEntity.ok(expenseService.getUserExpenses(userId));
    }
    
    // Get single expense
    @GetMapping("/{userId}/{expenseId}")
    public ResponseEntity<Expense> getExpense(
            @PathVariable String userId,
            @PathVariable String expenseId) {
        
        return ResponseEntity.ok(expenseService.getExpense(userId, expenseId));
    }
    
    // Update expense
    @PutMapping("/{userId}/{expenseId}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable String userId,
            @PathVariable String expenseId,
            @Valid @RequestBody ExpenseRequest request) {
        
        Expense expense = expenseService.updateExpense(
                userId, expenseId,
                request.getTitle(),
                request.getAmount(),
                request.getCategory(),
                request.getDescription()
        );
        
        return ResponseEntity.ok(expense);
    }
    
    // Delete expense
    @DeleteMapping("/{userId}/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable String userId,
            @PathVariable String expenseId) {
        
        expenseService.deleteExpense(userId, expenseId);
        return ResponseEntity.noContent().build();
    }
    
    // Get total expenses summary
    @GetMapping("/{userId}/summary/total")
    public ResponseEntity<Map<String, Object>> getTotalExpenses(@PathVariable String userId) {
        double total = expenseService.getTotalExpenses(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("totalExpenses", total);
        response.put("currency", "USD");
        return ResponseEntity.ok(response);
    }
    
    // Get expenses by category
    @GetMapping("/{userId}/category/{category}")
    public ResponseEntity<List<Expense>> getExpensesByCategory(
            @PathVariable String userId,
            @PathVariable String category) {
        
        return ResponseEntity.ok(expenseService.getExpensesByCategory(userId, category));
    }
    
    // Get category total
    @GetMapping("/{userId}/summary/category/{category}")
    public ResponseEntity<Map<String, Object>> getCategoryTotal(
            @PathVariable String userId,
            @PathVariable String category) {
        
        double total = expenseService.getCategoryTotal(userId, category);
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("category", category.toUpperCase());
        response.put("total", total);
        return ResponseEntity.ok(response);
    }
}

// Request DTO with Jakarta Validation
class ExpenseRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
    private String title;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;
    
    @NotBlank(message = "Category is required")
    private String category;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
