package com.example.filestorage.exception;

import com.example.filestorage.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({IllegalArgumentException.class, InvalidFileKeyException.class, ConstraintViolationException.class,
            MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiErrorResponse> badRequest(Exception ex, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, safeMessage(ex), request);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound(FileNotFoundException ex, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(StorageUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> unavailable(StorageUnavailableException ex, HttpServletRequest request) {
        log.warn("Storage service unavailable: {}", ex.getMessage());
        return response(HttpStatus.SERVICE_UNAVAILABLE, "Storage service is temporarily unavailable", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> unexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected request failure", ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred", request);
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String message, HttpServletRequest request) {
        ApiErrorResponse body = new ApiErrorResponse(Instant.now(), status.value(), status.name(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    private String safeMessage(Exception ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank() ? "Invalid request" : ex.getMessage();
    }
}
