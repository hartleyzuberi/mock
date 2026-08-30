package com.aceli.mock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(FundingRequestException.class)
    public ResponseEntity<ApiError> handleDomain(FundingRequestException ex) {
        return ResponseEntity.status(ex.status())
                .body(error(ex.status(), ex.code(), ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> details.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformed(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST",
                        "Request body contains an invalid or unsupported value", Map.of()));
    }

    private ApiError error(HttpStatus status, String code, String message, Map<String, String> details) {
        return new ApiError(Instant.now(), status.value(), code, message, details);
    }

    public record ApiError(Instant timestamp, int status, String code, String message, Map<String, String> details) {}
}
