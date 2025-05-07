package org.danniles.transformer;

import org.danniles.map.Column;
import org.danniles.map.StemmingFunction;
import java.io.IOException;
import java.util.UUID;
import org.apache.spark.ml.Transformer;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.util.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

public class Stemmer extends Transformer implements MLWritable {

    private final String uid;

    public Stemmer(String uid) {
        this.uid = uid;
    }

    public Stemmer() {
        this.uid = "CustomStemmer" + "_" + UUID.randomUUID().toString();
    }

    @Override
    public String uid() {
        return this.uid;
    }

    @Override
    public Dataset<Row> transform(Dataset<?> dataset) {
        // Create encoder from schema
        StructType outputSchema = this.transformSchema(dataset.schema());

        // Use map instead of mapPartitions with StemmingFunction
        return dataset.toDF().map(new StemmingFunction(), Encoders.row(outputSchema));
    }

    @Override
    public StructType transformSchema(StructType schema) {
        return new StructType(new StructField[]{
                Column.ID.getStructType(),
                Column.ROW_NUMBER.getStructType(),
                Column.LABEL.getStructType(),
                Column.STEMMED_WORD.getStructType()
        });
    }

    @Override
    public Transformer copy(ParamMap extra) {
        return super.defaultCopy(extra);
    }

    @Override
    public MLWriter write() {
        return new DefaultParamsWriter(this);
    }

    @Override
    public void save(String path) throws IOException {
        write().save(path);
    }

    public static MLReader<Stemmer> read() {
        return new DefaultParamsReader<>();
    }

}
