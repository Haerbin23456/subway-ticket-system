package com.subway.ticket.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handle(Exception e) {
        return ResponseEntity.status(500).body(new ErrorBody("INTERNAL_ERROR", e.getMessage()));
    }

    static class ErrorBody {
        public String code;
        public String message;
        public ErrorBody(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
