package com.ridelink.farepayment.exception;

public class ResourceNotFoundException extends RuntimeException {

    // Constructor accepting an error message
    public ResourceNotFoundException(String message) {
        super(message);
    }

}