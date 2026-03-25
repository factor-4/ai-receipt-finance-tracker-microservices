package com.kulubotti.expense_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulubotti.expense_service.entity.Expense;
import com.kulubotti.expense_service.model.ExpenseStatus;
import com.kulubotti.expense_service.repository.ExpenseRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseResultConsumer {

    private final ExpenseRepository expenseRepository;
    private final ObjectMapper objectMapper; // To parse the JSON result

    public ExpenseResultConsumer(ExpenseRepository expenseRepository, ObjectMapper objectMapper) {
        this.expenseRepository = expenseRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @KafkaListener(topics = "expense-results", groupId = "expense-group-v3") // Changed to v3 to ensure fresh start
    public void handleProcessingResult(String message) {
        try {
            System.out.println(" [KAFKA] Received raw message: " + message);
            JsonNode jsonNode = objectMapper.readTree(message);

            // SAFE ACCESS: Check if the node exists before converting to long
            JsonNode idNode = jsonNode.get("expenseId");
            if (idNode == null || idNode.isNull()) {
                System.err.println("[KAFKA] Field 'expenseId' is missing in JSON!");
                return;
            }

            Long id = idNode.asLong();
            String status = jsonNode.has("status") ? jsonNode.get("status").asText() : "UNKNOWN";

            expenseRepository.findById(id).ifPresentOrElse(expense -> {
                expense.setStatus(ExpenseStatus.PROCESSED);
                expenseRepository.saveAndFlush(expense);
                System.out.println(" [KAFKA] Database updated: Expense " + id + " is now PROCESSED.");
            }, () -> {
                System.err.println("[KAFKA] Received result for Expense ID " + id + " but it's not in our DB!");
            });

        } catch (Exception e) {
            System.err.println("[KAFKA] Error processing result: " + e.getMessage());
            e.printStackTrace();
        }
    }
}