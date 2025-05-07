package org.danniles.driver;

import java.util.HashMap;
import java.util.Map;

public enum Genre {

    POP("pop", 0D),

    COUNTRY("country", 1D),

    BLUES("blues", 2D),

    JAZZ("jazz", 3D),

    REGGAE("reggae", 4D),

    ROCK("rock", 5D),

    HIPHOP("hip hop", 6D),

    UNKNOWN("Don't know :(", -1D);

    private final String name;
    private final Double value;

    private static final Map<String, Genre> NAME_TO_GENRE = new HashMap<>();
    private static final Map<Double, Genre> VALUE_TO_GENRE = new HashMap<>();

    static {
        for (Genre genre : values()) {
            NAME_TO_GENRE.put(genre.name.toLowerCase(), genre);
            VALUE_TO_GENRE.put(genre.value, genre);
        }
    }

    Genre(final String name, final Double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Double getValue() {
        return value;
    }

    public static Genre fromName(String name) {
        if (name == null) {
            return UNKNOWN;
        }
        return NAME_TO_GENRE.getOrDefault(name.trim().toLowerCase(), UNKNOWN);
    }

    public static Genre fromValue(Double value) {
        if (value == null) {
            return UNKNOWN;
        }
        return VALUE_TO_GENRE.getOrDefault(value, UNKNOWN);
    }
}
