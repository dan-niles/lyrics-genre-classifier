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
import org.apache.spark.ml.tuning.ParamGridBuilder;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.danniles.driver.Genre;
import org.danniles.driver.transformer.*;
import org.danniles.map.Column;
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

@Component("FeedForwardNeuralNetworkPipeline")
public class FeedForwardNeuralNetworkPipeline extends CommonLyricsPipeline {

    public PipelineModel classify() {
        Dataset<Row> lyricsDataset = readLyrics();
        final long SEED = 1234L;

        // Create a temporary view of the dataset for use in other methods
        lyricsDataset.createOrReplaceTempView("lyrics_dataset");

        System.out.println("Dataset Schema: ");
        lyricsDataset.printSchema();

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
                .setMinCount(0)
                .setWindowSize(10)
                .setVectorSize(300);

        // Configure the neural network for multi-class classification
        // The output layer size should match the number of genres
        int[] layers = new int[]{300, 256, 128, 7};

        MultilayerPerceptronClassifier multilayerPerceptronClassifier = new MultilayerPerceptronClassifier()
                .setBlockSize(256)
                .setSeed(SEED)
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
                .addGrid(verser.sentencesInVerse(), new int[]{16})
                .addGrid(multilayerPerceptronClassifier.maxIter(), new int[]{3000})
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

        System.out.println("Unique label values:");
        lyricsDataset.groupBy("label").count().show();

        // Split dataset (80-20)
        Dataset<Row> shuffled = lyricsDataset.orderBy(rand(SEED));
        Dataset<Row>[] splits = shuffled.randomSplit(new double[]{0.8, 0.2}, SEED);
        Dataset<Row> trainingData = splits[0];
        Dataset<Row> testData = splits[1];

        double bestAccuracy = 0.0;
        PipelineModel bestModel = null;

        double trainAccuracy = 0;
        double testAccuracy = 0;
        for (ParamMap params : paramGrid) {
            // Set pipeline params
            PipelineModel model = pipeline.fit(trainingData, params);

            // Evaluate on train set
            Dataset<Row> predictions = model.transform(trainingData);
            trainAccuracy = evaluator.evaluate(predictions);
            System.out.println("Params: " + params + " -> Training Accuracy: " + trainAccuracy);

            // Evaluate on test set
            predictions = model.transform(testData);
            testAccuracy = evaluator.evaluate(predictions);
            System.out.println("Params: " + params + " -> Testing Accuracy: " + testAccuracy);

            if (testAccuracy > bestAccuracy) {
                bestAccuracy = testAccuracy;
                bestModel = model;
            }
        }

        System.out.println("Model Training Completed!");
        saveModel(bestModel, getModelDirectory());
        System.out.println("Model Saved in " + getModelDirectory());

        String stats = "Final Training Accuracy: " + trainAccuracy + System.lineSeparator() + "Final Testing Accuracy: " + testAccuracy + System.lineSeparator();

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
                    stats.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            System.out.println("Accuracy stats successfully written to " + accuracyFile.toString());
            System.out.println(stats);
        } catch (IOException e) {
            System.err.println("Failed to write accuracy stats: " + e.getMessage());
        }

        return bestModel;
    }

    public Map<String, Object> getModelStatistics(PipelineModel model) {
        Map<String, Object> modelStatistics = super.getModelStatistics(model);

        Transformer[] stages = model.stages();

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