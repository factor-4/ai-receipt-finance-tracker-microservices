package com.kulubotti.ai_parser_service.service;

import com.kulubotti.ai_parser_service.event.ExpenseCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ReceiptProcessor {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ReceiptProcessor(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    // This method automatically triggers the millisecond a message hits the topic!
    @KafkaListener(topics = "ai-receipt-topic", groupId = "ai-processing-group")
    public void processReceipt(ExpenseCreatedEvent event) {

        System.out.println(" [AI PARSER] Received task for Expense ID: " + event.expenseId());
        System.out.println(" [AI PARSER] User requesting scan: now  " + event.username());

        // --- MOCK AI PROCESSING DELAY ---
        try {
            System.out.println(" [AI PARSER] Analyzing image data... (simulating 3 second delay)");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Inside the listener method after the 3-second delay
        Map<String, Object> resultPayload = Map.of(
                "expenseId", event.expenseId(),
                "status", "PROCESSED"
        );

        // Send the Map directly - KafkaTemplate will convert it to perfect JSON
        kafkaTemplate.send("expense-results", resultPayload).whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println(" [AI PARSER] SUCCESS: Sent Expense ID " + event.expenseId());
            } else {
                System.err.println(" [AI PARSER] FAILED: " + ex.getMessage());
            }
        });


    }
}