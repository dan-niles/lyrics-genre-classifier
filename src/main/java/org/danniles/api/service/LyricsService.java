package org.danniles.api.service;

import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.danniles.GenrePrediction;
import org.danniles.pipeline.LyricsPipeline;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LyricsService {

    private final LyricsPipeline pipeline;

    public LyricsService(@Qualifier("word2VecPipeline") LyricsPipeline pipeline) {
        this.pipeline = pipeline;
    }

    public Map<String, Object> classifyLyrics() {
        CrossValidatorModel model = pipeline.classify();
        return pipeline.getModelStatistics(model);
    }

    public GenrePrediction predictGenre(final String unknownLyrics) {
        return pipeline.predict(unknownLyrics);
    }
}
