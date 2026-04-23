package com.aicbi.apigateway;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestControllerAdvice
public class ApiGatewayExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    var errors = ex.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(
            fe -> fe.getField(),
            fe -> fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage(),
            (a, b) -> b));
    return ResponseEntity.badRequest().body(Map.of(
        "timestamp", Instant.now().toString(),
        "error", "validation_failed",
        "details", errors));
  }

  @ExceptionHandler(WebClientResponseException.class)
  public ResponseEntity<Map<String, Object>> handleDownstream(WebClientResponseException ex) {
    return ResponseEntity.status(ex.getStatusCode()).body(Map.of(
        "timestamp", Instant.now().toString(),
        "error", "downstream_error",
        "status", ex.getStatusCode().value(),
        "message", ex.getResponseBodyAsString()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
        "timestamp", Instant.now().toString(),
        "error", "internal_error",
        "message", ex.getMessage() == null ? "unknown" : ex.getMessage()));
  }
}
