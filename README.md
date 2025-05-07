## Build Command

```bash
mvn clean package
```

## Run Command

```bash
java --add-exports java.base/sun.nio.ch=ALL-UNNAMED --add-opens java.base/sun.security.action=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED -jar spring-api/target/spring-api-1.0-SNAPSHOT.jar
```

```bash
java -jar spring-api/target/spring-api-1.0-SNAPSHOT.jar
```