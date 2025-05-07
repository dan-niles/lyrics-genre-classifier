package org.danniles.api.service;

import org.danniles.driver.pipeline.Word2VecPipeline;
import org.danniles.driver.word2vec.Similarity;
import org.danniles.driver.word2vec.Synonym;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class Word2VecService {
    @Autowired
    private Word2VecPipeline word2VecLyricsPipeline;

    public Map<String, Object> train() {
        return word2VecLyricsPipeline.train();
    }

    public List<Synonym> findSynonyms(String lyrics) {
        return word2VecLyricsPipeline.findSynonyms(lyrics);
    }

    public List<Similarity> calculateSimilarity(String lyrics) {
        return word2VecLyricsPipeline.calculateSimilarity(lyrics);
    }
}
