package com.kulubotti.expense_service.controller;

import com.kulubotti.expense_service.dto.ExpenseRequest;
import com.kulubotti.expense_service.entity.Expense;
import com.kulubotti.expense_service.model.ExpenseStatus;
import com.kulubotti.expense_service.repository.ExpenseRepository;
import com.kulubotti.expense_service.event.ExpenseCreatedEvent;
import com.kulubotti.expense_service.service.ExpenseProducer;
import com.kulubotti.expense_service.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> addExpense(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ExpenseRequest request) {

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        Expense newExpense = new Expense(
                username,
                request.merchantName(),
                request.amount(),
                request.date(),
                request.rawReceiptData(),
                ExpenseStatus.PENDING
        );

        // 1. Save to DB (Initial State)
        Expense savedExpense = expenseRepository.save(newExpense);

        // 2. Fire and Forget to Kafka
        ExpenseCreatedEvent event = new ExpenseCreatedEvent(savedExpense.getId(), username);
        expenseProducer.sendExpenseEvent(event);

        // 3. Return 202 Accepted
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Expense receipt uploaded successfully. AI processing has started.");
        response.put("id", savedExpense.getId());
        response.put("status", savedExpense.getStatus());
        response.put("checkStatusUrl", "/api/expenses/" + savedExpense.getId());

        return ResponseEntity.accepted().body(response);
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getMyExpenses(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        // This ensures the database query is scoped ONLY to the logged-in user
        List<Expense> myExpenses = expenseRepository.findByUsername(username);

        return ResponseEntity.ok(myExpenses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable Long id) {
        return expenseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}