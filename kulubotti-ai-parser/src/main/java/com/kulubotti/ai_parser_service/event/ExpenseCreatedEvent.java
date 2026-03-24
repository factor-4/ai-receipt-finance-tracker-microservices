package com.kulubotti.ai_parser_service.event;

// This MUST match the exact fields sent by the Expense Service
public record ExpenseCreatedEvent(Long expenseId, String username) {
}