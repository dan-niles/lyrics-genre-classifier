package org.danniles.driver.pipeline;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.Transformer;
import org.apache.spark.ml.classification.MultilayerPerceptronClassificationModel;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
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
import org.danniles.driver.Genre;
import org.danniles.driver.transformer.*;
import org.danniles.map.Column;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.danniles.map.Column.*;

@Component("FeedForwardNeuralNetworkPipeline")
public class FeedForwardNeuralNetworkPipeline extends CommonLyricsPipeline {

    public CrossValidatorModel classify() {
        Dataset<Row> lyricsDataset = readLyrics();

        // Create a temporary view of the dataset for use in other methods
        lyricsDataset.createOrReplaceTempView("lyrics_dataset");

        lyricsDataset.show(5);

        // Get the number of genres from the Genre enum (excluding UNKNOWN)
        int numGenres = Genre.values().length - 1; // Subtract 1 to exclude UNKNOWN
        System.out.println("Number of genres to classify: " + numGenres);

        // Remove all punctuation symbols.
        Cleanser cleanser = new Cleanser();

        // Add rowNumber based on it.
        Numerator numerator = new Numerator();

        // Split into words.
        Tokenizer tokenizer = new Tokenizer()
                .setInputCol(CLEAN.getName())
                .setOutputCol(WORDS.getName());

        // Remove stop words.
        StopWordsRemover stopWordsRemover = new StopWordsRemover()
                .setInputCol(WORDS.getName())
                .setOutputCol(FILTERED_WORDS.getName());

        // Create as many rows as words. This is needed or Stemmer.
        Exploder exploder = new Exploder();

        // Perform stemming.
        Stemmer stemmer = new Stemmer();

        Uniter uniter = new Uniter();
        Verser verser = new Verser();

        // Create model.
        Word2Vec word2Vec = new Word2Vec()
                .setInputCol(Column.VERSE.getName())
                .setOutputCol("features")
                .setMinCount(0);

        // Configure the neural network for multi-class classification
        // The output layer size should match the number of genres (7 genres + UNKNOWN)
        int[] layers = new int[]{300, 100, numGenres};

        MultilayerPerceptronClassifier multilayerPerceptronClassifier = new MultilayerPerceptronClassifier()
                .setBlockSize(300)
                .setSeed(1234L)
                .setLayers(layers);

        Pipeline pipeline = new Pipeline().setStages(
                new PipelineStage[]{
                        cleanser,
                        numerator,
                        tokenizer,
                        stopWordsRemover,
                        exploder,
                        stemmer,
                        uniter,
                        verser,
                        word2Vec,
                        multilayerPerceptronClassifier});

        // Use a ParamGridBuilder to construct a grid of parameters to search over.
        ParamMap[] paramGrid = new ParamGridBuilder()
                .addGrid(verser.sentencesInVerse(), new int[]{16, 24})
                .addGrid(word2Vec.vectorSize(), new int[] {300, 400})
                .addGrid(multilayerPerceptronClassifier.maxIter(), new int[] {100, 200})
                .build();

        // Use multiclass evaluator with the proper number of classes
        MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                .setLabelCol(LABEL.getName())
                .setPredictionCol("prediction")
                .setMetricName("accuracy");

        System.out.println("Genre mapping for classification:");
        for (Genre genre : Genre.values()) {
            if (genre != Genre.UNKNOWN) {
                System.out.println(genre.getName() + " -> " + genre.getValue());
            }
        }

        CrossValidator crossValidator = new CrossValidator()
                .setEstimator(pipeline)
                .setEvaluator(evaluator)
                .setEstimatorParamMaps(paramGrid)
                .setNumFolds(10);

        // Run cross-validation, and choose the best set of parameters.
        CrossValidatorModel model = crossValidator.fit(lyricsDataset);

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
        modelStatistics.put("Weights", ((MultilayerPerceptronClassificationModel) stages[9]).weights());

        // Add information about the neural network architecture
        MultilayerPerceptronClassificationModel nnModel = (MultilayerPerceptronClassificationModel) stages[9];
        modelStatistics.put("Neural Network Layers", nnModel.getLayers());

        // Add information about genre distribution in the training data if available
        try {
            Dataset<Row> currentData = sparkSession.table("lyrics_dataset");
            if (currentData != null) {
                Dataset<Row> genreCounts = currentData.groupBy(LABEL.getName()).count().orderBy("count");
                modelStatistics.put("Genre distribution", genreCounts.collectAsList());
            }
        } catch (Exception e) {
            System.out.println("Could not retrieve genre distribution: " + e.getMessage());
        }

        printModelStatistics(modelStatistics);

        return modelStatistics;
    }

    @Override
    protected String getModelDirectory() {
        return getLyricsModelDirectoryPath() + "/feed-forward-neural-network/";
    }
}