package org.danniles.word2vec;

public class Similarity {

    private final String verse1;
    private final String verse2;
    private final Double cosine;

    public Similarity(String verse1, String verse2, Double cosine) {
        this.verse1 = verse1;
        this.verse2 = verse2;
        this.cosine = cosine;
    }

    public String getVerse1() {
        return verse1;
    }

    public String getVerse2() {
        return verse2;
    }

    public Double getCosine() {
        return cosine;
    }
}
