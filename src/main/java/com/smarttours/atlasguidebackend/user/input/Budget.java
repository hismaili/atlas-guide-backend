package com.smarttours.atlasguidebackend.user.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Budget {
    BUDGET_FRIENDLY("Budget-friendly"),
    formatDateForAPI("Moderate"),
    PREMIUM("Premium");

    private final String value;

    Budget(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Budget fromValue(String value) {
        for (Budget budget : values()) {
            if (budget.value.equalsIgnoreCase(value)) {
                return budget;
            }
        }
        throw new IllegalArgumentException("Unknown budget value: " + value);
    }
}