package com.kulubotti.expense_service.controller;

import com.kulubotti.expense_service.dto.ExpenseRequest;
import com.kulubotti.expense_service.entity.Expense;
import com.kulubotti.expense_service.repository.ExpenseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseRepository expenseRepository;

    public ExpenseController(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @PostMapping
    public ResponseEntity<Expense> addExpense(
            @RequestHeader("X-Logged-In-User") String username,
            @RequestBody ExpenseRequest request) {

        Expense newExpense = new Expense(
                username,
                request.merchantName(),
                request.amount(),
                request.date(),
                request.rawReceiptData() // Passing the raw JSON straight to PostgreSQL
        );

        Expense savedExpense = expenseRepository.save(newExpense);
        return ResponseEntity.ok(savedExpense);
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getMyExpenses(
            @RequestHeader("X-Logged-In-User") String username) {

        List<Expense> myExpenses = expenseRepository.findByUsername(username);
        return ResponseEntity.ok(myExpenses);
    }
}