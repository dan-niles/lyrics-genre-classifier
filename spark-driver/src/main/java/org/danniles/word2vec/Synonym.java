package org.danniles.word2vec;

public class Synonym {

    private final String verse;
    private final Double cosine;

    public Synonym(String verse, Double cosine) {
        this.verse = verse;
        this.cosine = cosine;
    }

    public String getVerse() {
        return verse;
    }

    public Double getCosine() {
        return cosine;
    }
}
