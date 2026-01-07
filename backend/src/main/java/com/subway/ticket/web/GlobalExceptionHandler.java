package com.subway.ticket.web;

import com.subway.ticket.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorBody> handleBusiness(BusinessException e) {
        return ResponseEntity.status(400).body(new ErrorBody(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorBody> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(400).body(new ErrorBody("VALIDATION_ERROR", msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorBody> handle(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(new ErrorBody("INTERNAL_ERROR", "系统内部错误"));
    }

    public static class ErrorBody {
        public String code;
        public String message;
        public ErrorBody(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
