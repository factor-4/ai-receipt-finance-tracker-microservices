package com.kulubotti.expense_service.entity;

import com.kulubotti.expense_service.model.ExpenseStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    // Removed nullable = false because AI fills these in LATER
    private String merchantName;
    private BigDecimal amount;
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseStatus status;

    // The field for our professional image storage
    private String receiptImageUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> rawReceiptData;

    public Expense() {}

    // Updated Constructor
    public Expense(String username, String merchantName, BigDecimal amount, LocalDate date,
                   Map<String, Object> rawReceiptData, ExpenseStatus status) {
        this.username = username;
        this.merchantName = merchantName;
        this.amount = amount;
        this.date = date;
        this.rawReceiptData = rawReceiptData;
        this.status = status;
    }

    // --- Getters ---
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getMerchantName() { return merchantName; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public Map<String, Object> getRawReceiptData() { return rawReceiptData; }
    public ExpenseStatus getStatus() { return status; }
    public String getReceiptImageUrl() { return receiptImageUrl; }

    // --- Setters ---
    public void setStatus(ExpenseStatus status) { this.status = status; }
    public void setReceiptImageUrl(String receiptImageUrl) { this.receiptImageUrl = receiptImageUrl; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setRawReceiptData(Map<String, Object> rawReceiptData) { this.rawReceiptData = rawReceiptData; }
}