package com.ridelink.farepayment.exception;

public class InvalidFareException extends RuntimeException {

    // Constructor with an error message
    public InvalidFareException(String message) {
        super(message);
    }

    // Constructor with an error message and original exception
    public InvalidFareException(String message, Throwable cause) {
        super(message, cause);
    }
}