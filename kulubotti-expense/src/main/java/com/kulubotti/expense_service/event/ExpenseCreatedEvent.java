package com.kulubotti.expense_service.event;

public record ExpenseCreatedEvent(Long expenseId, String username) {
}