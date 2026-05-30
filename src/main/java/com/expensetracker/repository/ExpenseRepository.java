package com.expensetracker.repository;

import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    List<Expense> findByUserOrderByDateDesc(User user);
    
    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = :user AND YEAR(e.date) = :year AND MONTH(e.date) = :month")
    Double getMonthlyTotal(@Param("user") User user, @Param("year") int year, @Param("month") int month);
    
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.user = :user AND YEAR(e.date) = :year AND MONTH(e.date) = :month GROUP BY e.category")
    List<Object[]> getCategorySpending(@Param("user") User user, @Param("year") int year, @Param("month") int month);
}