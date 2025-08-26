package com.smarttours.atlasguidebackend.user.input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * An enum representing the available AI guide personas.
 * Each persona contains a display name for the UI/API and the detailed
 * instruction text to be inserted into the LLM prompt.
 */
public enum TourGuidePersona {

    FRIENDLY_LOCAL_EXPERT(
            "Friendly Local Expert",
            """
            Adopt a warm, conversational, and enthusiastic tone. Use contractions (e.g., "don't", "it's").
            Share practical 'insider tips' focused on avoiding crowds, finding authentic experiences,
            and great photo spots. Write as if you are talking to a friend.
            """
    ),

    KNOWLEDGEABLE_HISTORIAN(
            "Knowledgeable Historian",
            """
            Adopt a formal, academic, and precise tone. Prioritize historical accuracy, including specific
            dates, names, and architectural terms. Avoid slang and overly casual language.
            Write as if you are a knowledgeable museum curator leading a tour.
            """
    ),

    EFFICIENT_CONCIERGE(
            "Efficient Concierge",
            """
            Adopt a polished, professional, and direct tone. Focus on logistics, efficiency, and making the
            experience seamless. The language should be clear and concise. Provide tips that help save
            time and streamline the user's day.
            """
    ),

    ADVENTUROUS_STORYTELLER(
            "Adventurous Storyteller",
            """
            Adopt an evocative, engaging, and slightly dramatic tone. Weave a narrative and build excitement.
            Frame descriptions as part of a grand story. Use vivid language to bring the sights to life
            and make the user feel like they are on an epic journey.
            """
    );

    private final String displayName;
    private final String instructions;

    TourGuidePersona(String displayName, String instructions) {
        this.displayName = displayName;
        this.instructions = instructions;
    }

    /**
     * The user-friendly name, used for JSON serialization.
     * @return The display name string.
     */
    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    /**
     * The detailed instructions for the LLM prompt.
     * @return The instruction string.
     */
    public String getInstructions() {
        return instructions;
    }

    /**

     * Allows creating a Persona enum from a display name string (for JSON deserialization).
     */
    @JsonCreator
    public static TourGuidePersona fromValue(String value) {
        for (TourGuidePersona persona : values()) {
            if (persona.displayName.equalsIgnoreCase(value)) {
                return persona;
            }
        }
        throw new IllegalArgumentException("Unknown persona value: " + value);
    }
}