package com.kulubotti.expense_service.service;

import com.kulubotti.expense_service.event.ExpenseCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ExpenseProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ExpenseProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendExpenseEvent(ExpenseCreatedEvent event) {
        kafkaTemplate.send("ai-receipt-topic", event.username(), event);
        System.out.println("[KAFKA] Sent AI processing event for Expense ID: " + event.expenseId());
    }
}