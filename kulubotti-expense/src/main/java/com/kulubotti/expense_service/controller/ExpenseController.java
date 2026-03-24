package com.kulubotti.expense_service.controller;

import com.kulubotti.expense_service.dto.ExpenseRequest;
import com.kulubotti.expense_service.entity.Expense;
import com.kulubotti.expense_service.repository.ExpenseRepository;
import com.kulubotti.expense_service.event.ExpenseCreatedEvent;
import com.kulubotti.expense_service.service.ExpenseProducer;
import com.kulubotti.expense_service.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final ExpenseProducer expenseProducer;
    private final JwtService jwtService; // New Service to handle decoding

    public ExpenseController(ExpenseRepository expenseRepository,
                             ExpenseProducer expenseProducer,
                             JwtService jwtService) {
        this.expenseRepository = expenseRepository;
        this.expenseProducer = expenseProducer;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<Expense> addExpense(
            @RequestHeader("Authorization") String authHeader, // Pull the Token
            @RequestBody ExpenseRequest request) {

        // DYNAMIC: Extract username from "Bearer <token>"
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        Expense newExpense = new Expense(
                username,
                request.merchantName(),
                request.amount(),
                request.date(),
                request.rawReceiptData()
        );

        Expense savedExpense = expenseRepository.save(newExpense);

        ExpenseCreatedEvent event = new ExpenseCreatedEvent(savedExpense.getId(), username);
        expenseProducer.sendExpenseEvent(event);

        return ResponseEntity.ok(savedExpense);
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getMyExpenses(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        List<Expense> myExpenses = expenseRepository.findByUsername(username);
        return ResponseEntity.ok(myExpenses);
    }
}