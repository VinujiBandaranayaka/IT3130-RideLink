package com.ridelink.farepayment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Document(collection = "receipts")
public class Receipt {

    @Id
    private Long id;

    @Indexed(unique = true)
    private Long paymentId;

    @Indexed(unique = true)
    private String receiptNumber;

    private LocalDateTime issuedAt;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal amount;

    private Map<String, BigDecimal> breakdown;


    // Default constructor
    public Receipt() {
    }


    // Parameterized constructor
    public Receipt(
            Long id,
            Long paymentId,
            String receiptNumber,
            LocalDateTime issuedAt,
            BigDecimal amount,
            Map<String, BigDecimal> breakdown) {

        this.id = id;
        this.paymentId = paymentId;
        this.receiptNumber = receiptNumber;
        this.issuedAt = issuedAt;
        this.amount = amount;
        this.breakdown = breakdown;
    }


    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Map<String, BigDecimal> getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(
            Map<String, BigDecimal> breakdown) {

        this.breakdown = breakdown;
    }
}