package org.danniles.api.service;

import org.danniles.GenrePrediction;
import org.danniles.pipeline.LyricsPipeline;
import java.util.Map;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LyricsService {

    @Autowired
    private LyricsPipeline pipeline;

    public Map<String, Object> classifyLyrics() {
        CrossValidatorModel model = pipeline.classify();
        return pipeline.getModelStatistics(model);
    }

    public GenrePrediction predictGenre(final String unknownLyrics) {
        return pipeline.predict(unknownLyrics);
    }

}
