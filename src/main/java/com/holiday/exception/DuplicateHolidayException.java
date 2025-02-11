package com.holiday.exception;

public class DuplicateHolidayException extends RuntimeException{
    public DuplicateHolidayException(String message) {
        super(message);
    }
}
