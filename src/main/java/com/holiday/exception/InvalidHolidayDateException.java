package com.holiday.exception;

public class InvalidHolidayDateException extends RuntimeException{
    public InvalidHolidayDateException(String message) {
        super(message);
    }
}
