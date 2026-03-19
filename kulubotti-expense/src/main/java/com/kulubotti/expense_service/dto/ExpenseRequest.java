package com.kulubotti.expense_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record ExpenseRequest(
        String merchantName,
        BigDecimal amount,
        LocalDate date,
        Map<String, Object> rawReceiptData
) {}