# Lyrics Genre Classifier

This project extends the work of Taras Matyashovskyy by building a more robust music lyrics classification system using Apache Spark’s MLlib. While Taras’ original model could only classify “Pop” and “Metal,” this system can classify lyrics into eight distinct genres using a larger and more diverse dataset, a retrained ML pipeline, and an interactive web-based UI for visualization.

## Project Overview

Trained a machine learning pipeline using Spark MLlib to classify lyrics into the following genres:

- Pop
- Country
- Blues
- Jazz
- Reggae
- Rock
- Hip-Hop
- RnB

Built an interactive web interface for real-time lyric classification. Displayed classification results using dynamic visualizations.

## How to Run the Project

1. Clone the repository:

   ```bash
   git clone https://github.com/dan-niles/lyrics-genre-classifier
   ```

2. Navigate to the project directory:

   ```bash
    cd lyrics-genre-classifier
   ```

3. Ensure you have Java and Maven installed on your machine.
4. Build the project using Maven:

   ```bash
   mvn clean package
   ```

5. Run the application:

   ```bash
    java --add-exports java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/sun.security.action=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED -jar spring-api/target/spring-api-1.0-SNAPSHOT.jar
   ```

6. Run the React frontend:

   ```bash
   cd app
   npm install
   npm run dev
   ```

## Acknowledgements

This project builds upon the [work of Taras Matyashovskyy](https://github.com/tmatyashovsky/spark-ml-samples), whose original lyrics classification model served as a foundation. The [Mendeley dataset](https://data.mendeley.com/datasets/3t9vbwxgr5/2) was used for training and testing the model, providing a rich source of diverse lyrics across multiple genres.
