package com.smarttours.atlasguidebackend.domain.exceptions;

public class UncompleteItineraryException extends Exception {

    public UncompleteItineraryException(String message, Throwable cause) {
        super(message, cause);
    }

    public UncompleteItineraryException(Throwable cause) {
        super(cause);
    }

    public UncompleteItineraryException(String message) {
        super(message);
    }
}
