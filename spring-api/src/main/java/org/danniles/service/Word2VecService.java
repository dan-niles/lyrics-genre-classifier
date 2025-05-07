package org.danniles.service;

import org.danniles.pipeline.Word2VecPipeline;
import org.danniles.word2vec.Similarity;
import org.danniles.word2vec.Synonym;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class Word2VecService {

    private final Word2VecPipeline word2VecLyricsPipeline;

    public Word2VecService(Word2VecPipeline word2VecLyricsPipeline) {
        this.word2VecLyricsPipeline = word2VecLyricsPipeline;
    }

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
