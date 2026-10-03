package com.github.hiroshi85.bankx_reactive_ledger.common.error;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalErrorHandler {
    private static final Map<String, HttpStatus> STATUS = Map.of(
        "account_not_found", HttpStatus.NOT_FOUND,
        "risk_rejected", HttpStatus.UNPROCESSABLE_ENTITY,
        "insufficient_funds", HttpStatus.UNPROCESSABLE_ENTITY
    );

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiError> business(BusinessException error) {
        return ResponseEntity.status(
                STATUS.getOrDefault(error.code(), HttpStatus.UNPROCESSABLE_ENTITY)
            )
            .body(new ApiError(error.code()));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    ResponseEntity<ApiError> validation(WebExchangeBindException error) {
        return ResponseEntity.badRequest().body(new ApiError("validation_error"));
    }

    @ExceptionHandler(ServerWebInputException.class)
    ResponseEntity<ApiError> malformed(ServerWebInputException error) {
        return ResponseEntity.badRequest().body(new ApiError("validation_error"));
    }
}
