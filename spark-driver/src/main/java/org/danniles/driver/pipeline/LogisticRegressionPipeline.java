package org.danniles.driver.pipeline;

import org.apache.spark.ml.PipelineModel;
import org.danniles.map.Column;
import org.danniles.driver.transformer.*;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.feature.Word2Vec;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.ml.tuning.ParamGridBuilder;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import static org.apache.spark.sql.functions.rand;
import static org.danniles.map.Column.*;

@Component("LogisticRegressionPipeline")
public class LogisticRegressionPipeline extends CommonLyricsPipeline {

    @Override
    public PipelineModel classify() {
        Dataset<Row> lyricsDataset = readLyrics();
        final long SEED = 1234L;

        // Create a temporary view of the dataset for use in other methods
        lyricsDataset.createOrReplaceTempView("lyrics_dataset");

        // Split dataset (80-20)
        Dataset<Row> shuffled = lyricsDataset.orderBy(rand(SEED));
        Dataset<Row>[] splits = shuffled.randomSplit(new double[]{0.8, 0.2}, SEED);
        Dataset<Row> train = splits[0];
        Dataset<Row> test = splits[1];

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

        Word2Vec w2v = new Word2Vec()
                .setInputCol(Column.VERSE.getName())
                .setOutputCol("features")
                .setMinCount(1);

        LogisticRegression lr = new LogisticRegression()
                .setLabelCol(Column.LABEL.getName())
                .setFeaturesCol("features");

        Pipeline pipeline = new Pipeline().setStages(new PipelineStage[] {
                cleanser, numerator, tokenizer, stopWordsRemover, exploder,
                stemmer, uniter, verser, w2v, lr
        });

        ParamMap[] grid = new ParamGridBuilder()
                .addGrid(verser.sentencesInVerse(), new int[] { 4 })
                .addGrid(w2v.vectorSize(), new int[] { 150 })
                .addGrid(lr.regParam(), new double[] { 0.01 })
                .addGrid(lr.maxIter(), new int[] { 50 })
                .build();

        CrossValidator cv = new CrossValidator()
                .setEstimator(pipeline)
                .setEstimatorParamMaps(grid)
                .setEvaluator(
                        new MulticlassClassificationEvaluator()
                                .setLabelCol(Column.LABEL.getName())
                                .setPredictionCol("prediction")
                                .setMetricName("accuracy"))
                .setNumFolds(3);

        CrossValidatorModel cvModel = cv.fit(train);

        PipelineModel bestModel = (PipelineModel) cvModel.bestModel();

        Dataset<Row> predictionsOnTest = bestModel.transform(test);
        double accuracy = new MulticlassClassificationEvaluator()
                .setLabelCol(Column.LABEL.getName())
                .setPredictionCol("prediction")
                .setMetricName("accuracy")
                .evaluate(predictionsOnTest);
        System.out.println("Accuracy on test set: " + accuracy);

        saveModel(bestModel, getModelDirectory());

        // Use multiclass evaluator with the proper number of classes
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                .setLabelCol(LABEL.getName())
                .setPredictionCol("prediction")
                .setMetricName("accuracy");

        // Evaluate on train set
        Dataset<Row> predictions = bestModel.transform(train);
        double trainAccuracy = evaluator.evaluate(predictions);
        System.out.println("Training Accuracy: " + trainAccuracy);

        // Evaluate on test set
        predictions = bestModel.transform(test);
        double testAccuracy = evaluator.evaluate(predictions);
        System.out.println("Testing Accuracy: " + testAccuracy);

        String evalStats = "Final Training Accuracy: " + trainAccuracy + System.lineSeparator() + "Final Testing Accuracy: " + testAccuracy + System.lineSeparator();

        try {
            String modelDir = getModelDirectory();
            Path modelDirPath = Paths.get(modelDir);

            // Create directories if they don't exist
            if (!Files.exists(modelDirPath)) {
                Files.createDirectories(modelDirPath);
            }

            // Ensure path ends with proper separator and append filename
            if (!modelDir.endsWith(File.separator)) {
                modelDir += File.separator;
            }

            Path accuracyFile = Paths.get(modelDir + "model_accuracy.txt");

            // Write the stats to the file
            Files.write(
                    accuracyFile,
                    evalStats.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            System.out.println("Accuracy stats successfully written to " + accuracyFile.toString());
            System.out.println(evalStats);
        } catch (IOException e) {
            System.err.println("Failed to write accuracy stats: " + e.getMessage());
        }

        Map<String, Object> stats = getModelStatistics(cvModel);
        stats.put("testSetAccuracy", accuracy);
        printModelStatistics(stats);

        return bestModel;
    }

    @Override
    protected String getModelDirectory() {
        return getLyricsModelDirectoryPath() + "/logistic-regression/";
    }
}