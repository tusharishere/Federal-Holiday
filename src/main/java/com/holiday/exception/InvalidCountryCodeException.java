package com.holiday.exception;

public class InvalidCountryCodeException extends RuntimeException{
    public InvalidCountryCodeException(String message) {
        super(message);
    }
}
