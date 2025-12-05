package com.library.management.exception;

public class CsvHeaderMissingException extends RuntimeException{
    public CsvHeaderMissingException(String message) {
        super(message);
    }
}
