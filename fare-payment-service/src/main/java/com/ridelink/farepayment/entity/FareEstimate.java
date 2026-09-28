package com.ridelink.farepayment.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "fare_estimates")
public class FareEstimate {

    @Id
    private Long id;

    @Indexed
    private String rideId;

    private BigDecimal distanceKm;

    private BigDecimal durationMin;

    private BigDecimal estimatedAmount;

    private String currency;

    private LocalDateTime createdAt;


    // Empty constructor
    public FareEstimate() {
    }


    // Constructor
    public FareEstimate(
            Long id,
            String rideId,
            BigDecimal distanceKm,
            BigDecimal durationMin,
            BigDecimal estimatedAmount,
            String currency,
            LocalDateTime createdAt) {

        this.id = id;
        this.rideId = rideId;
        this.distanceKm = distanceKm;
        this.durationMin = durationMin;
        this.estimatedAmount = estimatedAmount;
        this.currency = currency;
        this.createdAt = createdAt;
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

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(BigDecimal distanceKm) {
        this.distanceKm = distanceKm;
    }

    public BigDecimal getDurationMin() {
        return durationMin;
    }

    public void setDurationMin(BigDecimal durationMin) {
        this.durationMin = durationMin;
    }

    public BigDecimal getEstimatedAmount() {
        return estimatedAmount;
    }

    public void setEstimatedAmount(BigDecimal estimatedAmount) {
        this.estimatedAmount = estimatedAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}