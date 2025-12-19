package org.writer.linkservice.exception;

public class NotFoundShortUrlException extends RuntimeException {
    public NotFoundShortUrlException(String code) {
        super("Short URL not found: " + code);
    }
}
