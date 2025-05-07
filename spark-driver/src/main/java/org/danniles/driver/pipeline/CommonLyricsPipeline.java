package org.danniles.driver.pipeline;

import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.linalg.DenseVector;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.danniles.driver.Genre;
import org.danniles.driver.GenrePrediction;
import org.danniles.driver.MLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.danniles.map.Column.*;
import static org.apache.spark.sql.functions.*;

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
                .withColumn(LABEL.getName(), functions.lit(Genre.UNKNOWN.getValue()))
                .withColumn(ID.getName(), functions.lit("unknown.txt"));

        CrossValidatorModel model = mlService.loadCrossValidationModel(getModelDirectory());
        getModelStatistics(model);

        PipelineModel bestModel = (PipelineModel) model.bestModel();

        Dataset<Row> predictionsDataset = bestModel.transform(unknownLyricsDataset);
        Row predictionRow = predictionsDataset.first();

        System.out.println("\n------------------------------------------------");
        final Double prediction = predictionRow.getAs("prediction");
        System.out.println("Prediction: " + Double.toString(prediction));

        if (Arrays.asList(predictionsDataset.columns()).contains("probability")) {
            final DenseVector probability = predictionRow.getAs("probability");
            System.out.println("Probability: " + probability);
            System.out.println("------------------------------------------------\n");

            return new GenrePrediction(getGenre(prediction).getName(), probability.apply(0), probability.apply(1));
        }

        System.out.println("------------------------------------------------\n");
        return new GenrePrediction(getGenre(prediction).getName());
    }

    Dataset<Row> readLyrics() {
        // Define explicit schema for the required columns
        StructType schema = DataTypes.createStructType(new StructField[] {
                DataTypes.createStructField("artist_name", DataTypes.StringType, true),
                DataTypes.createStructField("track_name", DataTypes.StringType, true),
                DataTypes.createStructField("release_date", DataTypes.StringType, true),
                DataTypes.createStructField("genre", DataTypes.StringType, true),
                DataTypes.createStructField("lyrics", DataTypes.StringType, true)
        });

        // Read CSV with defined schema and options
        Dataset<Row> rawData = sparkSession.read()
                .option("header", "true")
                .option("mode", "DROPMALFORMED")
                .option("nullValue", "")
                .option("multiLine", "true") // Handle multi-line lyrics fields
                .option("escape", "\"")
                .option("quote", "\"")
                .schema(schema)
                .csv(lyricsTrainingSetDirectoryPath);

        // Filter out records with null or empty lyrics
        Dataset<Row> filteredData = rawData
                .filter("lyrics IS NOT NULL AND length(trim(lyrics)) > 0")
                .withColumn("release_date", year(to_date(col("release_date"), "yyyy-MM-dd"))) // Extract year from date
                .withColumnRenamed("release_date", "year");

        // Convert genre column to numeric label for ML
        Dataset<Row> labeledData = filteredData
                .withColumn(LABEL.getName(), genreToLabel(filteredData.col("genre")))
                .withColumn(ID.getName(), functions.monotonically_increasing_id().cast("string"));

        // Cache the dataset for performance
        return labeledData.coalesce(sparkSession.sparkContext().defaultMinPartitions()).cache();
    }

    private Column genreToLabel(Column genreCol) {
        sparkSession.udf().register("genreToLabelUDF", (String genreName) ->
                Genre.fromName(genreName).getValue(), DataTypes.DoubleType);
        return functions.callUDF("genreToLabelUDF", genreCol);
    }

    private Dataset<Row> readLyrics(String inputDirectory, String path) {
        Dataset<String> rawLyrics = sparkSession.read().textFile(Paths.get(inputDirectory).resolve(path).toString());
        rawLyrics = rawLyrics.filter(rawLyrics.col(VALUE.getName()).notEqual(""));
        rawLyrics = rawLyrics.filter(rawLyrics.col(VALUE.getName()).contains(" "));

        // Add source filename column as a unique id.
        return rawLyrics.withColumn(ID.getName(), functions.input_file_name());
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
    public Map<String, Object> getModelStatistics(CrossValidatorModel model) {
        Map<String, Object> modelStatistics = new HashMap<>();

        Arrays.sort(model.avgMetrics());
        modelStatistics.put("Best model metrics", model.avgMetrics()[model.avgMetrics().length - 1]);

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