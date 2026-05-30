package com.expensetracker.controller;

import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ExpenseController {
    
    @Autowired
    private ExpenseRepository expenseRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        
        // Get all expenses
        List<Expense> allExpenses = expenseRepository.findByUserOrderByDateDesc(user);
        model.addAttribute("expenses", allExpenses);
        
        // Current month and year
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        
        // Get current month expenses
        List<Expense> monthlyExpenses = allExpenses.stream()
                .filter(e -> e.getDate().getYear() == currentYear && e.getDate().getMonthValue() == currentMonth)
                .collect(Collectors.toList());
        
        // Calculate monthly total
        double monthlyTotal = monthlyExpenses.stream().mapToDouble(Expense::getAmount).sum();
        model.addAttribute("monthlyTotal", monthlyTotal);
        
        // Expense count
        model.addAttribute("expenseCount", allExpenses.size());
        
        // Daily average
        int daysInMonth = now.lengthOfMonth();
        double dailyAverage = monthlyTotal / daysInMonth;
        model.addAttribute("dailyAverage", Math.round(dailyAverage * 100.0) / 100.0);
        
        // Category spending for pie chart
        Map<String, Double> categorySpending = new HashMap<>();
        for (Expense expense : monthlyExpenses) {
            String categoryName = expense.getCategory().name();
            categorySpending.put(categoryName, 
                categorySpending.getOrDefault(categoryName, 0.0) + expense.getAmount());
        }
        model.addAttribute("categorySpending", categorySpending);
        model.addAttribute("categoryLabels", new ArrayList<>(categorySpending.keySet()));
        model.addAttribute("categoryData", new ArrayList<>(categorySpending.values()));
        
        // Weekly spending for line chart (last 7 days)
        Map<String, Double> weeklySpending = new LinkedHashMap<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE");
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = now.minusDays(i);
            String dayName = date.format(dayFormatter);
            double dayTotal = allExpenses.stream()
                    .filter(e -> e.getDate().equals(date))
                    .mapToDouble(Expense::getAmount)
                    .sum();
            weeklySpending.put(dayName, dayTotal);
        }
        model.addAttribute("weeklyLabels", new ArrayList<>(weeklySpending.keySet()));
        model.addAttribute("weeklyData", new ArrayList<>(weeklySpending.values()));
        
        // Categories for dropdown
        model.addAttribute("categories", Category.values());
        
        // Current date
        model.addAttribute("currentDate", now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        
        return "dashboard";
    }
    
    @PostMapping("/expense/add")
    public String addExpense(@RequestParam String title,
                             @RequestParam(required = false) String description,
                             @RequestParam Double amount,
                             @RequestParam Category category,
                             @RequestParam(required = false) LocalDate date,
                             @AuthenticationPrincipal UserDetails userDetails) {
        
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        
        Expense expense = new Expense();
        expense.setTitle(title);
        expense.setDescription(description != null && !description.isEmpty() ? description : "");
        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setDate(date != null ? date : LocalDate.now());
        expense.setUser(user);
        
        expenseRepository.save(expense);
        
        return "redirect:/dashboard?success=true";
    }
    
    @GetMapping("/expense/delete/{id}")
    public String deleteExpense(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        
        Expense expense = expenseRepository.findById(id).orElse(null);
        if (expense != null && expense.getUser().getId().equals(user.getId())) {
            expenseRepository.delete(expense);
        }
        
        return "redirect:/dashboard";
    }
}