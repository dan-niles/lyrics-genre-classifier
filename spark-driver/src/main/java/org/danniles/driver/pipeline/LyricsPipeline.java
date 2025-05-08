package org.danniles.driver.pipeline;

import org.apache.spark.ml.PipelineModel;
import org.danniles.driver.GenrePrediction;

import java.util.Map;

public interface LyricsPipeline {

    PipelineModel classify();

    GenrePrediction predict(String unknownLyrics);

    Map<String, Object> getModelStatistics(PipelineModel model);

}
