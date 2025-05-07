package org.danniles.pipeline;

import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.danniles.GenrePrediction;

import java.util.Map;

public interface LyricsPipeline {

    CrossValidatorModel classify();

    GenrePrediction predict(String unknownLyrics);

    Map<String, Object> getModelStatistics(CrossValidatorModel model);

}
