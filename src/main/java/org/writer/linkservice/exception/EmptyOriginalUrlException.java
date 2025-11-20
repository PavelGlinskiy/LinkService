package org.writer.linkservice.exception;

public class EmptyOriginalUrlException extends RuntimeException {

    public EmptyOriginalUrlException() {
        super("Original URL must not be empty");
    }

    public EmptyOriginalUrlException(String message) {
        super(message);
    }
}
