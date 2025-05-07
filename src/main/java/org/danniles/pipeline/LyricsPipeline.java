package org.danniles.pipeline;

import org.danniles.GenrePrediction;
import java.util.Map;
import org.apache.spark.ml.tuning.CrossValidatorModel;

public interface LyricsPipeline {

    CrossValidatorModel classify();

    GenrePrediction predict(String unknownLyrics);

    Map<String, Object> getModelStatistics(CrossValidatorModel model);

}
