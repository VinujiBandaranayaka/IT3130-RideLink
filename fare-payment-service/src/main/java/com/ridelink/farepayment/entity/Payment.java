package com.ridelink.farepayment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Document(collection = "payments")
public class Payment {

    // MongoDB document ID
    @Id
    private Long id;

    // Ride and user identifiers
    @Indexed
    private String rideId;

    private String passengerId;

    private String driverId;

    // Payment information
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal amount;

    private String currency;

    private PaymentStatus status;

    private PaymentMethod method;

    @Indexed(unique = true)
    private String transactionRef;

    // Timestamps
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    // Default constructor
    public Payment() {
    }


    // Parameterized constructor
    public Payment(
            Long id,
            String rideId,
            String passengerId,
            String driverId,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            PaymentMethod method,
            String transactionRef,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.rideId = rideId;
        this.passengerId = passengerId;
        this.driverId = driverId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.method = method;
        this.transactionRef = transactionRef;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}