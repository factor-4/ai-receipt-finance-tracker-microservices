package com.kulubotti.expense_service.repository;

import com.kulubotti.expense_service.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUsername(String username);
}