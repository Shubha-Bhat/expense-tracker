package com.expensetracker.service;

import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExpenseService {
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    public Expense saveExpense(Expense expense) {
        return expenseRepository.save(expense);
    }
    
    public List<Expense> getUserExpenses(User user) {
        return expenseRepository.findByUserOrderByDateDesc(user);
    }
    
    public Double getCurrentMonthTotal(User user) {
        LocalDate now = LocalDate.now();
        Double total = expenseRepository.getMonthlyTotal(user, now.getYear(), now.getMonthValue());
        return total != null ? total : 0.0;
    }
    
    public Map<String, Double> getCategorySpending(User user) {
        LocalDate now = LocalDate.now();
        List<Object[]> results = expenseRepository.getCategorySpending(user, now.getYear(), now.getMonthValue());
        
        Map<String, Double> categoryMap = new HashMap<>();
        for (Object[] result : results) {
            categoryMap.put(result[0].toString(), (Double) result[1]);
        }
        return categoryMap;
    }
    
    public void deleteExpense(Long id, User user) {
        Expense expense = expenseRepository.findById(id).orElse(null);
        if (expense != null && expense.getUser().getId().equals(user.getId())) {
            expenseRepository.delete(expense);
        }
    }
    
}