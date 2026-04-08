package com.jamel.expense_tracker.service;

import org.springframework.stereotype.Service;

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
    
    public List<Expense> getUserExpenses(String userId) {
        return expenseRepository.findByUserId(userId);
    }
    
    public Expense getExpense(String userId, String expenseId) {
        Expense expense = expenseRepository.findById(userId, expenseId);
        if (expense == null) {
            throw new RuntimeException("Expense not found with ID: " + expenseId);
        }
        return expense;
    }
    
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
    
    public void deleteExpense(String userId, String expenseId) {
        if (!expenseRepository.exists(userId, expenseId)) {
            throw new RuntimeException("Expense not found with ID: " + expenseId);
        }
        expenseRepository.delete(userId, expenseId);
    }
    
    public double getTotalExpenses(String userId) {
        return getUserExpenses(userId).stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }
    
    public List<Expense> getExpensesByCategory(String userId, String category) {
        return getUserExpenses(userId).stream()
                .filter(expense -> expense.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }
    
    public double getCategoryTotal(String userId, String category) {
        return getExpensesByCategory(userId, category).stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }
}
