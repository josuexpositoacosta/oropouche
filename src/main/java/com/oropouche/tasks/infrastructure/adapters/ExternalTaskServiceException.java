package com.oropouche.tasks.infrastructure.adapters;

public class ExternalTaskServiceException extends RuntimeException {

    public ExternalTaskServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}