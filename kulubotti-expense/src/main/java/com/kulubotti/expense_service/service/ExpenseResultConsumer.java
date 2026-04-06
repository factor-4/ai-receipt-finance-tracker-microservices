package com.kulubotti.expense_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulubotti.expense_service.entity.Expense;
import com.kulubotti.expense_service.model.ExpenseStatus;
import com.kulubotti.expense_service.repository.ExpenseRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Service
public class ExpenseResultConsumer {

    private final ExpenseRepository expenseRepository;
    private final ObjectMapper objectMapper;

    public ExpenseResultConsumer(ExpenseRepository expenseRepository, ObjectMapper objectMapper) {
        this.expenseRepository = expenseRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @KafkaListener(topics = "expense-results", groupId = "expense-group-v3")
    public void handleProcessingResult(String message) {
        try {
            System.out.println(" [KAFKA] Received raw message: " + message);
            JsonNode jsonNode = objectMapper.readTree(message);

            JsonNode idNode = jsonNode.get("expenseId");
            if (idNode == null || idNode.isNull()) {
                System.err.println("[KAFKA] Field 'expenseId' is missing in JSON!");
                return;
            }

            Long id = idNode.asLong();
            String status = jsonNode.has("status") ? jsonNode.get("status").asText() : "UNKNOWN";

            expenseRepository.findById(id).ifPresentOrElse(expense -> {

                // If it was successful AND we have the aiData field
                if ("PROCESSED".equals(status) && jsonNode.has("aiData")) {
                    String rawAiData = jsonNode.get("aiData").asText();

                    // Clean the Markdown from the string
                    String cleanJson = rawAiData.replace("```json", "").replace("```", "").trim();

                    try {
                        // Parse the inner JSON string into a usable tree
                        JsonNode aiDataNode = objectMapper.readTree(cleanJson);

                        // Map the structured fields safely
                        if (aiDataNode.hasNonNull("merchantName")) {
                            expense.setMerchantName(aiDataNode.get("merchantName").asText());
                        }
                        if (aiDataNode.hasNonNull("amount")) {
                            // Converts the AI's number/string into your BigDecimal
                            expense.setAmount(new BigDecimal(aiDataNode.get("amount").asText()));
                        }
                        if (aiDataNode.hasNonNull("date")) {
                            // Converts the AI's "YYYY-MM-DD" into your LocalDate
                            expense.setDate(LocalDate.parse(aiDataNode.get("date").asText()));
                        }

                        // Map the entire raw JSON to your JSONB column
                        // We convert the JSON tree into a Java Map as required by your entity
                        @SuppressWarnings("unchecked")
                        Map<String, Object> rawMap = objectMapper.convertValue(aiDataNode, Map.class);
                        expense.setRawReceiptData(rawMap);

                        expense.setStatus(ExpenseStatus.PROCESSED);
                        System.out.println(" [KAFKA] Saved AI data for Expense " + id + ".");

                    } catch (Exception e) {
                        System.err.println(" [KAFKA] Failed to parse AI Data: " + e.getMessage());
                        // If parsing fails, we still save, but mark it failed so a human can check
                        expense.setStatus(ExpenseStatus.FAILED);
                    }
                } else {
                    expense.setStatus(ExpenseStatus.FAILED);
                }

                expenseRepository.saveAndFlush(expense);

            }, () -> {
                System.err.println("[KAFKA] Received result for Expense ID " + id + " but it's not in our DB!");
            });

        } catch (Exception e) {
            System.err.println("[KAFKA] Error processing result: " + e.getMessage());
            e.printStackTrace();
        }
    }
}