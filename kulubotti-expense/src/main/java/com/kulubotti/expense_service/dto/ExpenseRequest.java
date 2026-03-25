package com.kulubotti.expense_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record ExpenseRequest(
        @NotBlank(message = "Merchant name is required")
        String merchantName,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Date is required")
        LocalDate date,

        Map<String, Object> rawReceiptData
) {}