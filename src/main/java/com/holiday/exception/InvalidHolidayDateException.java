package com.holiday.exception;

public class InvalidHolidayDateException extends RuntimeException{
    public InvalidHolidayDateException(String message) {
        super(message);
    }

    public InvalidHolidayDateException(String message, Throwable cause) {
        super(message, cause);
    }
}
