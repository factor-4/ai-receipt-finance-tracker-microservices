package com.kulubotti.expense_service.controller;

import com.kulubotti.expense_service.dto.ExpenseRequest;
import com.kulubotti.expense_service.entity.Expense;
import com.kulubotti.expense_service.exception.ResourceNotFoundException;
import com.kulubotti.expense_service.model.ExpenseStatus;
import com.kulubotti.expense_service.repository.ExpenseRepository;
import com.kulubotti.expense_service.event.ExpenseCreatedEvent;
import com.kulubotti.expense_service.service.CloudinaryService;
import com.kulubotti.expense_service.service.ExpenseProducer;
import com.kulubotti.expense_service.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final ExpenseProducer expenseProducer;
    private final JwtService jwtService;
    private final CloudinaryService cloudinaryService;

    public ExpenseController(ExpenseRepository expenseRepository,
                             ExpenseProducer expenseProducer,
                             JwtService jwtService, CloudinaryService cloudinaryService) {
        this.expenseRepository = expenseRepository;
        this.expenseProducer = expenseProducer;
        this.jwtService = jwtService;
        this.cloudinaryService=cloudinaryService;
    }




    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadExpense(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file) throws IOException {

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        // 1. REAL CLOUD UPLOAD - No more mocks!
        String realImageUrl = cloudinaryService.uploadImage(file);

        // 2. Save the real URL to DB
        Expense newExpense = new Expense(
                username,
                "Analyzing...",
                BigDecimal.ZERO,
                LocalDate.now(),
                null,
                ExpenseStatus.PENDING
        );
        newExpense.setReceiptImageUrl(realImageUrl);
        Expense savedExpense = expenseRepository.save(newExpense);

        // 3. Send the REAL URL to the AI Service via Kafka
        ExpenseCreatedEvent event = new ExpenseCreatedEvent(
                savedExpense.getId(),
                username,
                realImageUrl
        );
        expenseProducer.sendExpenseEvent(event);

        return ResponseEntity.accepted().body(Map.of(
                "message", "Image uploaded to Cloud. AI analysis started.",
                "url", realImageUrl
        ));
    }


    @PostMapping
    public ResponseEntity<Map<String, Object>> addExpense(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ExpenseRequest request) {

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

        Expense savedExpense = expenseRepository.save(newExpense);

        // Basic event without URL
        ExpenseCreatedEvent event = new ExpenseCreatedEvent(savedExpense.getId(), username, null);
        expenseProducer.sendExpenseEvent(event);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Manual expense added. AI processing started.");
        response.put("id", savedExpense.getId());
        response.put("status", savedExpense.getStatus());

        return ResponseEntity.accepted().body(response);
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getMyExpenses(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        return ResponseEntity.ok(expenseRepository.findByUsername(username));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        return ResponseEntity.ok(expense);
    }
}