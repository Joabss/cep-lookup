package com.challenge.ceplookup.exception;

import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Java 21: ErrorResponse como record — imutável, sem boilerplate.
     */
    public record ErrorResponse(int status, String error, String message, LocalDateTime timestamp) {
        static ErrorResponse of(HttpStatus status, String message) {
            return new ErrorResponse(status.value(), status.getReasonPhrase(), message, LocalDateTime.now());
        }
    }

    @ExceptionHandler(CepNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(CepNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(HttpStatus.BAD_REQUEST, "CEP inválido: deve conter 8 dígitos numéricos"));
    }

    /**
     * Java 21: pattern matching com switch expression para mapear exceções a status HTTP.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        HttpStatus status = switch (ex) {
            case IllegalArgumentException e -> HttpStatus.BAD_REQUEST;
            case UnsupportedOperationException e -> HttpStatus.NOT_IMPLEMENTED;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status, "Erro: " + ex.getMessage()));
    }
}
