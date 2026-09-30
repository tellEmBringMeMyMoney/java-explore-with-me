package ru.practicum.ewm.main.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReactionType {
    FIRE("fire"), AHH("ahh");
    private final String value;
    ReactionType(String value) { this.value = value; }
    @JsonValue public String getValue() { return value; }
    @JsonCreator public static ReactionType fromValue(String value) {
        for (ReactionType type : values()) if (type.value.equalsIgnoreCase(value)) return type;
        throw new IllegalArgumentException("Reaction must be fire or ahh");
    }
}
