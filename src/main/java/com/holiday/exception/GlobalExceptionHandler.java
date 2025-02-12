package com.holiday.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DuplicateHolidayException.class)
    public ResponseEntity<String> handleDuplicateHolidayException(DuplicateHolidayException ex) {
        logger.warn("Handling Duplicate Holiday Exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidHolidayDateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidHolidayDateException(InvalidHolidayDateException ex) {
        logger.warn("Handling Invalid Holiday Date Exception: {}", ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", "Invalid date format");
        response.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Handling Illegal Argument Exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        logger.error("Internal Server Error: ", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Please Put Valid date format - dd-MM-yyyy.");
    }
}
