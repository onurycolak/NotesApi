package com.onur.bootcamp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Urgency { // enums are ordered by their declaration order, with .ordinal get value
    LOW, MEDIUM, HIGH;

    @JsonCreator
    public static Urgency fromString(String value) {
        return value == null ? null : Urgency.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
