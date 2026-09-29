package com.ridelink.account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================
    // ACCOUNT NOT FOUND
    // =========================
    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleAccountNotFound(
            AccountNotFoundException exception
    ) {
        return Map.of(
                "status", 404,
                "error", "Not Found",
                "message", exception.getMessage()
        );
    }


    // =========================
    // DUPLICATE EMAIL
    // =========================
    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleEmailAlreadyExists(
            EmailAlreadyExistsException exception
    ) {
        return Map.of(
                "status", 409,
                "error", "Conflict",
                "message", exception.getMessage()
        );
    }


    // =========================
    // VALIDATION ERRORS
    // =========================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationException(
            MethodArgumentNotValidException exception
    ) {

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", "Validation failed",
                "fields", errors
        );
    }


    // =========================
    // INVALID INPUT / STATUS
    // =========================
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        return Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", exception.getMessage()
        );
    }


    // =========================
    // AUTHENTICATION ERRORS
    // =========================
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleRuntimeException(
            RuntimeException exception
    ) {
        return Map.of(
                "status", 401,
                "error", "Unauthorized",
                "message", exception.getMessage()
        );
    }
}