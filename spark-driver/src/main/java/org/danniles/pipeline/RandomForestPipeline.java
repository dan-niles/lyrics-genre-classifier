package org.danniles.pipeline;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.Transformer;
import org.apache.spark.ml.classification.RandomForestClassificationModel;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.feature.Word2Vec;
import org.apache.spark.ml.feature.Word2VecModel;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.ml.tuning.ParamGridBuilder;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.danniles.transformer.*;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.danniles.map.Column.*;

@Component("RandomForestPipeline")
public class RandomForestPipeline extends CommonLyricsPipeline {

    public CrossValidatorModel classify() {
        Dataset<Row> sentences = readLyrics();

        Cleanser cleanser = new Cleanser();
        Numerator numerator = new Numerator();

        Tokenizer tokenizer = new Tokenizer()
                .setInputCol(CLEAN.getName())
                .setOutputCol(WORDS.getName());

        StopWordsRemover stopWordsRemover = new StopWordsRemover()
                .setInputCol(WORDS.getName())
                .setOutputCol(FILTERED_WORDS.getName());

        Exploder exploder = new Exploder();
        Stemmer stemmer = new Stemmer();
        Uniter uniter = new Uniter();
        Verser verser = new Verser();

        Word2Vec word2Vec = new Word2Vec().setInputCol(VERSE.getName()).setOutputCol("features").setMinCount(0);

        RandomForestClassifier randomForest = new RandomForestClassifier();

        Pipeline pipeline = new Pipeline().setStages(new PipelineStage[]{
                cleanser,
                numerator,
                tokenizer,
                stopWordsRemover,
                exploder,
                stemmer,
                uniter,
                verser,
                word2Vec,
                randomForest
        });

        ParamMap[] paramGrid = new ParamGridBuilder()
                .addGrid(verser.sentencesInVerse(), new int[]{16})
                .addGrid(word2Vec.vectorSize(), new int[]{300})
                .addGrid(randomForest.numTrees(), new int[]{10, 20, 30})
                .addGrid(randomForest.maxDepth(), new int[]{20, 30})
                .addGrid(randomForest.maxBins(), new int[]{32, 64, 128})
                .build();

        CrossValidator crossValidator = new CrossValidator()
                .setEstimator(pipeline)
                .setEvaluator(new BinaryClassificationEvaluator())
                .setEstimatorParamMaps(paramGrid)
                .setNumFolds(10);

        CrossValidatorModel model = crossValidator.fit(sentences);

        saveModel(model, getModelDirectory());

        return model;
    }

    public Map<String, Object> getModelStatistics(CrossValidatorModel model) {
        Map<String, Object> modelStatistics = super.getModelStatistics(model);

        PipelineModel bestModel = (PipelineModel) model.bestModel();
        Transformer[] stages = bestModel.stages();

        modelStatistics.put("Sentences in verse", ((Verser) stages[7]).getSentencesInVerse());
        modelStatistics.put("Word2Vec vocabulary", ((Word2VecModel) stages[8]).getVectors().count());
        modelStatistics.put("Vector size", ((Word2VecModel) stages[8]).getVectorSize());
        modelStatistics.put("Num trees", ((RandomForestClassificationModel) stages[9]).getNumTrees());
        modelStatistics.put("Max bins", ((RandomForestClassificationModel) stages[9]).getMaxBins());
        modelStatistics.put("Max depth", ((RandomForestClassificationModel) stages[9]).getMaxDepth());

        printModelStatistics(modelStatistics);

        return modelStatistics;
    }

    @Override
    protected String getModelDirectory() {
        return getLyricsModelDirectoryPath() + "/random-forest/";
    }
}
