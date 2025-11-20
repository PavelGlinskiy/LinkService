package org.writer.linkservice.exception;

public class AliasAlreadyInUseException extends RuntimeException {

    public AliasAlreadyInUseException(String alias) {
        super("Alias already in use: " + alias);
    }
}
