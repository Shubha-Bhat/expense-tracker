package com.expensetracker.controller;

import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
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


    // =========================
    // DASHBOARD
    // =========================

    @GetMapping("/dashboard")
    public String dashboard(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userRepository
                .findByEmail(userDetails.getUsername())
                .orElse(null);

        if (user == null) {
            return "redirect:/login";
        }


        // Get all expenses of logged-in user
        List<Expense> allExpenses =
                expenseRepository.findByUserOrderByDateDesc(user);

        model.addAttribute("expenses", allExpenses);


        // =========================
        // CURRENT DATE
        // =========================

        LocalDate now = LocalDate.now();

        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();


        // =========================
        // CURRENT MONTH EXPENSES
        // =========================

        List<Expense> monthlyExpenses = allExpenses.stream()
                .filter(e ->
                        e.getDate() != null &&
                        e.getDate().getYear() == currentYear &&
                        e.getDate().getMonthValue() == currentMonth
                )
                .collect(Collectors.toList());


        // =========================
        // MONTHLY TOTAL
        // =========================

        double monthlyTotal = monthlyExpenses.stream()
                .filter(e -> e.getAmount() != null)
                .mapToDouble(Expense::getAmount)
                .sum();

        model.addAttribute("monthlyTotal", monthlyTotal);


        // =========================
        // EXPENSE COUNT
        // =========================

        model.addAttribute("expenseCount", allExpenses.size());


        // =========================
        // DAILY AVERAGE
        // =========================

        int daysInMonth = now.lengthOfMonth();

        double dailyAverage = 0;

        if (daysInMonth > 0) {
            dailyAverage = monthlyTotal / daysInMonth;
        }

        dailyAverage =
                Math.round(dailyAverage * 100.0) / 100.0;

        model.addAttribute("dailyAverage", dailyAverage);


        // =========================
        // CATEGORY SPENDING
        // =========================

        Map<String, Double> categorySpending =
                new LinkedHashMap<>();

        for (Expense expense : monthlyExpenses) {

            if (expense.getCategory() == null ||
                    expense.getAmount() == null) {
                continue;
            }

            String categoryName =
                    expense.getCategory().name();

            double currentAmount =
                    categorySpending.getOrDefault(
                            categoryName,
                            0.0
                    );

            categorySpending.put(
                    categoryName,
                    currentAmount + expense.getAmount()
            );
        }

        model.addAttribute(
                "categorySpending",
                categorySpending
        );

        model.addAttribute(
                "categoryLabels",
                new ArrayList<>(
                        categorySpending.keySet()
                )
        );

        model.addAttribute(
                "categoryData",
                new ArrayList<>(
                        categorySpending.values()
                )
        );


        // =========================
        // LAST 7 DAYS SPENDING
        // =========================

        Map<String, Double> weeklySpending =
                new LinkedHashMap<>();

        DateTimeFormatter dayFormatter =
                DateTimeFormatter.ofPattern("dd MMM");


        /*
         * Create exactly 7 days.
         *
         * Example:
         *
         * 21 Sep
         * 22 Sep
         * 23 Sep
         * 24 Sep
         * 25 Sep
         * 26 Sep
         * 27 Sep
         *
         * Even if there is no expense on a day,
         * that day will have value 0.
         */

        for (int i = 6; i >= 0; i--) {

            LocalDate date =
                    now.minusDays(i);

            String dateLabel =
                    date.format(dayFormatter);

            double dayTotal =
                    allExpenses.stream()
                            .filter(e ->
                                    e.getDate() != null &&
                                    e.getDate().equals(date)
                            )
                            .filter(e ->
                                    e.getAmount() != null
                            )
                            .mapToDouble(
                                    Expense::getAmount
                            )
                            .sum();

            weeklySpending.put(
                    dateLabel,
                    dayTotal
            );
        }


        model.addAttribute(
                "weeklyLabels",
                new ArrayList<>(
                        weeklySpending.keySet()
                )
        );

        model.addAttribute(
                "weeklyData",
                new ArrayList<>(
                        weeklySpending.values()
                )
        );


        // =========================
        // CATEGORIES
        // =========================

        model.addAttribute(
                "categories",
                Category.values()
        );


        // =========================
        // CURRENT DATE DISPLAY
        // =========================

        model.addAttribute(
                "currentDate",
                now.format(
                        DateTimeFormatter.ofPattern(
                                "EEEE, MMMM d, yyyy"
                        )
                )
        );


        return "dashboard";
    }


    // =========================
    // ADD EXPENSE
    // =========================

    @PostMapping("/expense/add")
    public String addExpense(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam Double amount,
            @RequestParam Category category,
            @RequestParam(required = false) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        User user =
                userRepository
                        .findByEmail(
                                userDetails.getUsername()
                        )
                        .orElse(null);

        if (user == null) {
            return "redirect:/login";
        }


        Expense expense = new Expense();

        expense.setTitle(title);

        expense.setDescription(
                description != null &&
                !description.trim().isEmpty()
                        ? description
                        : ""
        );

        expense.setAmount(amount);

        expense.setCategory(category);

        expense.setDate(
                date != null
                        ? date
                        : LocalDate.now()
        );

        expense.setUser(user);


        expenseRepository.save(expense);


        return "redirect:/dashboard?success=true";
    }


    // =========================
    // DELETE EXPENSE
    // =========================

    @GetMapping("/expense/delete/{id}")
    public String deleteExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        User user =
                userRepository
                        .findByEmail(
                                userDetails.getUsername()
                        )
                        .orElse(null);

        if (user == null) {
            return "redirect:/login";
        }


        Expense expense =
                expenseRepository
                        .findById(id)
                        .orElse(null);


        /*
         * Important:
         * User can delete only their own expense.
         */

        if (expense != null &&
                expense.getUser() != null &&
                expense.getUser().getId()
                        .equals(user.getId())) {

            expenseRepository.delete(expense);
        }


        return "redirect:/dashboard";
    }


    // =========================
    // DOWNLOAD RESULTS
    // =========================

    @GetMapping("/expenses/download")
    public void downloadExpenses(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response)
            throws IOException {

        if (userDetails == null) {

            response.sendRedirect("/login");

            return;
        }


        User user =
                userRepository
                        .findByEmail(
                                userDetails.getUsername()
                        )
                        .orElse(null);


        if (user == null) {

            response.sendRedirect("/login");

            return;
        }


        // Get only logged-in user's expenses
        List<Expense> expenses =
                expenseRepository
                        .findByUserOrderByDateDesc(user);


        // Tell browser this is a CSV file
        response.setContentType("text/csv");

        response.setCharacterEncoding("UTF-8");

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=expenseflow-results.csv"
        );


        PrintWriter writer =
                response.getWriter();


        // CSV header
        writer.println(
                "Date,Title,Description,Category,Amount"
        );


        // CSV rows
        for (Expense expense : expenses) {

            String date =
                    expense.getDate() != null
                            ? expense.getDate().toString()
                            : "";

            String title =
                    expense.getTitle() != null
                            ? expense.getTitle()
                            : "";

            String description =
                    expense.getDescription() != null
                            ? expense.getDescription()
                            : "";

            String category =
                    expense.getCategory() != null
                            ? expense.getCategory().name()
                            : "";

            String amount =
                    expense.getAmount() != null
                            ? expense.getAmount().toString()
                            : "0";


            writer.println(
                    escapeCsv(date) + "," +
                    escapeCsv(title) + "," +
                    escapeCsv(description) + "," +
                    escapeCsv(category) + "," +
                    escapeCsv(amount)
            );
        }


        writer.flush();
    }


    // =========================
    // CSV ESCAPE
    // =========================

    private String escapeCsv(String value) {

        if (value == null) {
            return "";
        }


        /*
         * Replace:
         *
         * "  -> ""
         *
         * Then put the complete value
         * inside double quotes.
         */

        value = value.replace(
                "\"",
                "\"\""
        );


        return "\"" + value + "\"";
    }
}