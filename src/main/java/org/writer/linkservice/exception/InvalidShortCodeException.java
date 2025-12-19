package org.writer.linkservice.exception;

public class InvalidShortCodeException extends RuntimeException {
    public InvalidShortCodeException(String code) {
        super("Invalid short code: " + code);
    }
}
