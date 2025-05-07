package org.danniles.pipeline;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.feature.Word2Vec;
import org.apache.spark.ml.feature.Word2VecModel;
import org.apache.spark.ml.linalg.DenseVector;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.sql.*;
import org.danniles.Genre;
import org.danniles.transformer.*;
import org.danniles.word2vec.Similarity;
import org.danniles.word2vec.Synonym;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.danniles.map.Column.*;

@Component
@Qualifier("word2VecPipeline")
public class Word2VecPipeline extends CommonLyricsPipeline {

    private final SparkSession sparkSession;

    public Word2VecPipeline(SparkSession sparkSession,
                            @Value("${lyrics.model.directory.path}") String modelPath) {
        this.sparkSession = sparkSession;
        setLyricsModelDirectoryPath(modelPath);
    }

    public Map<String, Object> train() {
        Dataset<Row> sentences = readLyrics();

        Pipeline pipeline = new Pipeline().setStages(new PipelineStage[]{
                new Cleanser(),
                new Numerator(),
                new Tokenizer().setInputCol(CLEAN.getName()).setOutputCol(WORDS.getName()),
                new StopWordsRemover().setInputCol(WORDS.getName()).setOutputCol(FILTERED_WORDS.getName()),
                new Exploder(),
                new Stemmer(),
                new Uniter(),
                new Verser(),
                new Word2Vec()
                        .setInputCol(VERSE.getName())
                        .setOutputCol("features")
                        .setVectorSize(300)
                        .setMinCount(0)
        });

        PipelineModel pipelineModel = pipeline.fit(sentences);
        saveModel(pipelineModel, getModelDirectory());

        Word2VecModel word2VecModel = (Word2VecModel) pipelineModel
                .stages()[pipelineModel.stages().length - 1];

        Map<String, Object> modelStats = new HashMap<>();
        modelStats.put("Word2Vec vectors count", word2VecModel.getVectors().count());

        return modelStats;
    }

    public List<Synonym> findSynonyms(String word) {
        PipelineModel model = PipelineModel.load(getModelDirectory());
        Word2VecModel vecModel = Arrays.stream(model.stages())
                .filter(stage -> stage instanceof Word2VecModel)
                .map(stage -> (Word2VecModel) stage)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Word2VecModel not found"));

        Dataset<Row> synonyms = vecModel.findSynonyms(word, 5);
        return synonyms.collectAsList().stream()
                .map(row -> new Synonym(row.getString(0), row.getDouble(1)))
                .collect(Collectors.toList());
    }

    public List<Similarity> calculateSimilarity(String lyrics) {
        String[] verses = lyrics.split("\\r?\\n");

        Dataset<String> versesDataset = sparkSession.createDataset(
                Arrays.asList(verses), Encoders.STRING());

        Dataset<Row> dataset = versesDataset
                .withColumn(LABEL.getName(), functions.lit(Genre.UNKNOWN.getValue()))
                .withColumn(ID.getName(), functions.lit("unknown.txt"));

        PipelineModel model = PipelineModel.load(getModelDirectory());
        Dataset<Row> word2Vec = model.transform(dataset);
        List<Row> features = word2Vec.select("verse", "features").collectAsList();

        List<Similarity> results = new ArrayList<>();
        for (Row a : features) {
            for (Row b : features) {
                results.add(new Similarity(
                        getVerse(a),
                        getVerse(b),
                        cosineSimilarity(getVector(a), getVector(b))
                ));
            }
        }
        return results;
    }

    private double[] getVector(Row row) {
        return ((DenseVector) row.getAs("features")).toArray();
    }

    private String getVerse(Row row) {
        @SuppressWarnings("unchecked")
        List<String> verseList = row.getList(row.fieldIndex("verse"));
        return String.join(" ", verseList);
    }

    private double cosineSimilarity(double[] a, double[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += Math.pow(a[i], 2);
            normB += Math.pow(b[i], 2);
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    @Override
    public String getModelDirectory() {
        return getLyricsModelDirectoryPath() + "/word2vec/";
    }

    @Override
    public CrossValidatorModel classify() {
        throw new UnsupportedOperationException("Classification is not supported for Word2Vec pipeline.");
    }
}
