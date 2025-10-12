package com.smarttours.atlasguidebackend.domain.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ItineraryPersitenceException extends Exception {
    public ItineraryPersitenceException(Throwable e) {
        super(e);
    }
}
