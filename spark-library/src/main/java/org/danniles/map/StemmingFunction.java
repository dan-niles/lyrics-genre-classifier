package org.danniles.map;

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.danniles.map.snowball.SnowballStemmer;

public class StemmingFunction implements MapFunction<Row, Row> {

    private final SnowballStemmer stemmer = initializeStemmer();

    private SnowballStemmer initializeStemmer() {
        try {
            Class<?> stemClass = Class.forName("org.danniles.map.snowball.ext.englishStemmer");
            return (SnowballStemmer) stemClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Row call(Row input) throws Exception {
        stemmer.setCurrent(input.getAs(Column.FILTERED_WORD.getName()));
        stemmer.stem();
        String stemmedWord = stemmer.getCurrent();

        return RowFactory.create(
                input.get(input.schema().fieldIndex(Column.ID.getName())),
                input.getInt(input.schema().fieldIndex(Column.ROW_NUMBER.getName())),
                input.getDouble(input.schema().fieldIndex(Column.LABEL.getName())),
                stemmedWord);
    }

}
