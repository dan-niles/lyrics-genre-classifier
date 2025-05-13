package org.danniles.api.service;

import jakarta.annotation.Resource;
import org.apache.spark.ml.PipelineModel;
import org.danniles.driver.GenrePrediction;
import org.danniles.driver.pipeline.LyricsPipeline;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LyricsService {

    @Resource(name = "LogisticRegressionPipeline")
    private LyricsPipeline pipeline;

    public Map<String, Object> classifyLyrics() {
        PipelineModel model = pipeline.classify();
        return pipeline.getModelStatistics(model);
    }

    public GenrePrediction predictGenre(final String unknownLyrics) {
        return pipeline.predict(unknownLyrics);
    }

}