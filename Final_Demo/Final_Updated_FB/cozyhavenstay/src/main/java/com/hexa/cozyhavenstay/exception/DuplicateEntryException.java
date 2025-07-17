// src/main/java/com/hexa/cozyhavenstay/exception/DuplicateEntryException.java
package com.hexa.cozyhavenstay.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // This annotation automatically sets the HTTP status code to 409 Conflict
public class DuplicateEntryException extends RuntimeException {
    public DuplicateEntryException(String message) {
        super(message);
    }

    public DuplicateEntryException(String message, Throwable cause) {
        super(message, cause);
    }
}