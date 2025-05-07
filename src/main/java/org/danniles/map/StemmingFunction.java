package org.danniles.map;

import opennlp.tools.stemmer.PorterStemmer;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;

public class StemmingFunction implements MapFunction<Row, Row> {

    private final PorterStemmer stemmer = new PorterStemmer();

    @Override
    public Row call(Row input) throws Exception {
        String word = input.getAs(Column.FILTERED_WORD.getName());
        String stemmedWord = stemmer.stem(word);

        return RowFactory.create(
                input.get(input.schema().fieldIndex(Column.ID.getName())),
                input.getInt(input.schema().fieldIndex(Column.ROW_NUMBER.getName())),
                input.getDouble(input.schema().fieldIndex(Column.LABEL.getName())),
                stemmedWord);
    }
}