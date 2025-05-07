package org.danniles.driver;

public enum Genre {

    METAL("Metal \\m//", 0D),

    POP("Pop <(^.^)/", 1D),

    UNKNOWN("Don't know :(", -1D);

    private final String name;
    private final Double value;

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

}
