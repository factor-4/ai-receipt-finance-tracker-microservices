package com.kulubotti.expense_service.entity;

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

    @Column(nullable = false)
    private String merchantName;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;


    //  to store this Java Map natively as a PostgreSQL JSONB column!
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> rawReceiptData;

    public Expense() {}

    public Expense(String username, String merchantName, BigDecimal amount, LocalDate date, Map<String, Object> rawReceiptData) {
        this.username = username;
        this.merchantName = merchantName;
        this.amount = amount;
        this.date = date;
        this.rawReceiptData = rawReceiptData;
    }

    // --- Getters ---
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getMerchantName() { return merchantName; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public Map<String, Object> getRawReceiptData() { return rawReceiptData; }
}