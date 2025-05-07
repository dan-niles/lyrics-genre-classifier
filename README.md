## Build Command

```bash
mvn clean package
```

## Run Command

```bash
java --add-exports java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED -jar spring-api/target/spring-api-1.0-SNAPSHOT.jar
```