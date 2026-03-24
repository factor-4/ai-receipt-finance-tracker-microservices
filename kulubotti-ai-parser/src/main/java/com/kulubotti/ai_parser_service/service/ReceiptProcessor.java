package com.kulubotti.ai_parser_service.service;

import com.kulubotti.ai_parser_service.event.ExpenseCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ReceiptProcessor {

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

        System.out.println("[AI PARSER] Successfully extracted data for Expense ID: " + event.expenseId());


    }
}