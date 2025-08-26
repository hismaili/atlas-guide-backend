package com.smarttours.atlasguidebackend.user.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Pace {
    RELAXED("Relaxed"),
    BALANCED("Balanced"),
    ACTION_PACKED("Action-Packed");

    private final String value;

    Pace(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Pace fromValue(String value) {
        for (Pace pace : values()) {
            if (pace.value.equalsIgnoreCase(value)) {
                return pace;
            }
        }
        throw new IllegalArgumentException("Unknown pace value: " + value);
    }
}