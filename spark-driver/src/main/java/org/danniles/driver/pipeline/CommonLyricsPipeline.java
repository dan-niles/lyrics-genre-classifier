package org.danniles.driver.pipeline;

import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.linalg.DenseVector;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.sql.*;
import org.danniles.driver.Genre;
import org.danniles.driver.GenrePrediction;
import org.danniles.driver.MLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.apache.spark.sql.functions.*;
import static org.danniles.map.Column.*;

public abstract class CommonLyricsPipeline implements LyricsPipeline {

    @Autowired
    protected SparkSession sparkSession;

    @Autowired
    private MLService mlService;

    @Value("${lyrics.training.set.directory.path}")
    private String lyricsTrainingSetDirectoryPath;

    @Value("${lyrics.model.directory.path}")
    private String lyricsModelDirectoryPath;

    @Override
    public GenrePrediction predict(final String unknownLyrics) {
        String[] lyrics = unknownLyrics.split("\\r?\\n");
        Dataset<String> lyricsDataset = sparkSession.createDataset(Arrays.asList(lyrics),
                Encoders.STRING());

        Dataset<Row> unknownLyricsDataset = lyricsDataset
                .withColumnRenamed("value", VALUE.getName())
                .withColumn(LABEL.getName(), functions.lit(Genre.UNKNOWN.getValue()))
                .withColumn(ID.getName(), functions.lit("unknown.txt"));

        unknownLyricsDataset.show();

        PipelineModel model = mlService.loadPipelineModel(getModelDirectory());
        getModelStatistics(model);

        Dataset<Row> predictionsDataset = model.transform(unknownLyricsDataset);
        Row predictionRow = predictionsDataset.first();

        System.out.println("\n------------------------------------------------");
        final Double prediction = predictionRow.getAs("prediction");
        System.out.println("Prediction: " + Double.toString(prediction));

        if (Arrays.asList(predictionsDataset.columns()).contains("probability")) {
            final DenseVector probability = predictionRow.getAs("probability");
            System.out.println("Probability: " + probability);
            System.out.println("------------------------------------------------\n");

            return new GenrePrediction(getGenre(prediction).getName(), probability.apply(0), probability.apply(1), probability.apply(2), probability.apply(3), probability.apply(4), probability.apply(5), probability.apply(6), probability.apply(7));
        }

        System.out.println("------------------------------------------------\n");
        return new GenrePrediction(getGenre(prediction).getName());
    }

    Dataset<Row> readLyrics() {

        // Read CSV with defined schema and options
        Dataset<Row> rawData = sparkSession.read()
                .option("header", "true")
                .option("mode", "DROPMALFORMED")
                .option("nullValue", "")
                .option("multiLine", "true") // Handle multi-line lyrics fields
                .csv(lyricsTrainingSetDirectoryPath)
                .select(
                    col("release_date"),
                    col(GENRE.getName()),
                    col("lyrics")
                );

        System.out.println("Sample of raw table:");
        rawData.show(5);

        // Filter out records with null or empty lyrics
        Dataset<Row> filteredData = rawData
                .filter(col("lyrics").isNotNull().and(length(trim(col("lyrics"))).gt(0)))
                .withColumn("year", col("release_date").cast("integer"))
                .drop("release_date");

        System.out.println("Sample of filtered table:");
        filteredData.show(5);

        // Convert genre column to numeric label for ML
        Dataset<Row> labeledData = filteredData
                .withColumn(LABEL.getName(), genreToLabel(filteredData.col(GENRE.getName())))
                .withColumn(ID.getName(), functions.monotonically_increasing_id().cast("string"));

        System.out.println("Final columns: " + Arrays.toString(labeledData.columns()));

        System.out.println("Sample of label column:");
        labeledData.show(5);

        // Cache the dataset for performance
        return labeledData.coalesce(sparkSession.sparkContext().defaultMinPartitions()).cache();
    }

    private Column genreToLabel(Column genreCol) {
        return when(col(GENRE.getName()).equalTo(Genre.POP.getName()), Genre.POP.getValue())
                .when(col(GENRE.getName()).equalTo(Genre.COUNTRY.getName()), Genre.COUNTRY.getValue())
                .when(col(GENRE.getName()).equalTo(Genre.BLUES.getName()), Genre.BLUES.getValue())
                .when(col(GENRE.getName()).equalTo(Genre.JAZZ.getName()), Genre.JAZZ.getValue())
                .when(col(GENRE.getName()).equalTo(Genre.REGGAE.getName()), Genre.REGGAE.getValue())
                .when(col(GENRE.getName()).equalTo(Genre.ROCK.getName()), Genre.ROCK.getValue())
                .when(col(GENRE.getName()).equalTo(Genre.HIPHOP.getName()), Genre.HIPHOP.getValue())
                .when(col(GENRE.getName()).equalTo(Genre.RNB.getName()), Genre.RNB.getValue())
                .otherwise(Genre.UNKNOWN.getValue());
    }

    private Genre getGenre(Double value) {
        for (Genre genre: Genre.values()){
            if (genre.getValue().equals(value)) {
                return genre;
            }
        }
        return Genre.UNKNOWN;
    }

    @Override
    public Map<String, Object> getModelStatistics(PipelineModel model) {
        Map<String, Object> modelStatistics = new HashMap<>();

//        Arrays.sort(model.avgMetrics());
//        modelStatistics.put("Best model metrics", model.avgMetrics()[model.avgMetrics().length - 1]);

        modelStatistics.put("Best model metrics", model.hashCode());

        return modelStatistics;
    }

    void printModelStatistics(Map<String, Object> modelStatistics) {
        System.out.println("\n------------------------------------------------");
        System.out.println("Model statistics:");
        System.out.println(modelStatistics);
        System.out.println("------------------------------------------------\n");
    }

    void saveModel(CrossValidatorModel model, String modelOutputDirectory) {
        this.mlService.saveModel(model, modelOutputDirectory);
    }

    void saveModel(PipelineModel model, String modelOutputDirectory) {
        this.mlService.saveModel(model, modelOutputDirectory);
    }

    public void setLyricsTrainingSetDirectoryPath(String lyricsTrainingSetDirectoryPath) {
        this.lyricsTrainingSetDirectoryPath = lyricsTrainingSetDirectoryPath;
    }

    public void setLyricsModelDirectoryPath(String lyricsModelDirectoryPath) {
        this.lyricsModelDirectoryPath = lyricsModelDirectoryPath;
    }

    protected abstract String getModelDirectory();

    String getLyricsModelDirectoryPath() {
        return lyricsModelDirectoryPath;
    }
}