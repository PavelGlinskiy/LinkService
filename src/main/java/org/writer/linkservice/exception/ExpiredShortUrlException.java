package org.writer.linkservice.exception;

public class ExpiredShortUrlException extends RuntimeException {
    public ExpiredShortUrlException(String code) {
        super("Short URL expired: " + code);
    }
}
