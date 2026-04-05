package com.kulubotti.ai_parser_service.service;

import com.kulubotti.ai_parser_service.event.ExpenseCreatedEvent;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
public class ReceiptProcessor {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ChatModel chatModel;

    public ReceiptProcessor(KafkaTemplate<String, Object> kafkaTemplate, ChatModel chatModel) {
        this.kafkaTemplate = kafkaTemplate;
        this.chatModel = chatModel;
    }

    @KafkaListener(topics = "ai-receipt-topic", groupId = "ai-processing-group")
    public void processReceipt(ExpenseCreatedEvent event) {
        String instructions = "Extract merchantName, amount, date, and lineItems from this receipt as JSON.";

        try {
            //  Build Media using a URI
            var media = Media.builder()
                    .mimeType(MimeTypeUtils.IMAGE_PNG)
                    .data(URI.create(event.receiptImageUrl()))
                    .build();

            // Build UserMessage with text + media
            var userMessage = UserMessage.builder()
                    .text(instructions)
                    .media(List.of(media))
                    .build();

            // Call Gemini
            var response = chatModel.call(new Prompt(userMessage));
            String aiResponse = response.getResult().getOutput().getText();

            System.out.println("found ai response "+ aiResponse);

            //  Send results back to Kafka
            kafkaTemplate.send("expense-results", Map.of(
                    "expenseId", event.expenseId(),
                    "status", "PROCESSED",
                    "aiData", aiResponse
            ));

        } catch (Exception e) {
            System.err.println("[AI PARSER] Error: ");
            e.printStackTrace();
            kafkaTemplate.send("expense-results", Map.of(
                    "expenseId", event.expenseId(),
                    "status", "FAILED"
            ));
        }
    }
}