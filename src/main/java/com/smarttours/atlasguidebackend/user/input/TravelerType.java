package com.smarttours.atlasguidebackend.user.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TravelerType {
    SOLO("Solo"),
    COUPLE("Couple"),
    FAMILY_WITH_YOUNG_CHILDREN("Family with young children"),
    FAMILY_WITH_TEENAGERS("Family with teenagers"),
    FRIENDS("Friends"),
    ELDERLY_PARENTS("With elderly parents");

    private final String value;

    TravelerType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TravelerType fromValue(String value) {
        for (TravelerType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown traveler type value: " + value);
    }
}