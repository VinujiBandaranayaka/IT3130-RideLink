package com.ridelink.ride.exception;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception) {

        Map<String, Object> error = buildError(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .collect(Collectors.joining(", "))
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException exception) {

        return ResponseEntity.badRequest().body(
                buildError(
                        HttpStatus.BAD_REQUEST,
                        "Validation failed",
                        exception.getMessage()
                )
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            HttpMessageNotReadableException exception) {

        return ResponseEntity.badRequest().body(
                buildError(
                        HttpStatus.BAD_REQUEST,
                        "Malformed request body",
                        "Request body could not be parsed"
                )
        );
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            IllegalArgumentException exception) {

        return ResponseEntity.badRequest().body(
                buildError(
                        HttpStatus.BAD_REQUEST,
                        "Invalid request",
                        exception.getMessage()
                )
        );
    }

    @ExceptionHandler({IllegalStateException.class})
    public ResponseEntity<Map<String, Object>> handleConflict(
            IllegalStateException exception) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                buildError(
                        HttpStatus.CONFLICT,
                        "Invalid ride state",
                        exception.getMessage()
                )
        );
    }

    @ExceptionHandler(RideNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRideNotFound(
            RideNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                buildError(
                        HttpStatus.NOT_FOUND,
                        "Ride not found",
                        exception.getMessage()
                )
        );
    }

    @ExceptionHandler(DriverServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleDriverServiceUnavailable(
            DriverServiceUnavailableException exception) {

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                buildError(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Driver service unavailable",
                        "The driver service is temporarily unavailable"
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(
            Exception exception) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                buildError(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error",
                        "An unexpected error occurred"
                )
        );
    }

    private Map<String, Object> buildError(
            HttpStatus status,
            String error,
            String message) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);
        return response;
    }
}