package org.writer.linkservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmptyOriginalUrlException.class)
    public ResponseEntity<String> handleEmptyOriginalUrl(EmptyOriginalUrlException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(AliasAlreadyInUseException.class)
    public ResponseEntity<String> handleAliasAlreadyInUse(AliasAlreadyInUseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(NotFoundShortUrlException.class)
    public ResponseEntity<String> handleNotFoundShortUrl(NotFoundShortUrlException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ExpiredShortUrlException.class)
    public ResponseEntity<String> handleExpiredShortUrl(ExpiredShortUrlException ex) {
        return ResponseEntity.status(HttpStatus.GONE).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidShortCodeException.class)
    public ResponseEntity<String> handleInvalidShortCode(InvalidShortCodeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

}
