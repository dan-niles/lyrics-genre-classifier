package org.danniles;

import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource("classpath:spark.properties")
@ComponentScan("org.danniles.*")
public class SparkContextConfiguration {

    @Value("${spark.master}")
    private String master;

    @Value("${spark.application-name}")
    private String applicationName;

    @Value("${spark.distributed-libraries:}") // Optional; empty default if not set
    private String distributedLibraries;

    @Value("${spark.driver.memory}")
    private String driverMemory;

    @Value("${spark.serializer}")
    private String serializer;

    @Value("${spark.kryoserializer.buffer.max}")
    private String kryoserializerBufferMax;

    @Value("${spark.default.parallelism}")
    private String defaultParallelism;

    @Value("${spark.sql.shuffle.partitions}")
    private String sqlShufflePartitions;

    @Bean
    public SparkSession sparkSession() {
        SparkSession.Builder builder;

        try {
            builder = SparkSession.builder()
                    .appName(applicationName)
                    .master(master)
                    // Disable UI completely to avoid servlet dependencies
                    .config("spark.ui.enabled", "false")
                    // Disable metrics servlet
                    .config("spark.metrics.enabled", "false")
                    // Additional configs to avoid HttpServlet dependencies
                    .config("spark.metrics.conf.*.sink.servlet.class", "")
                    .config("spark.metrics.conf.*.sink.servlet.enabled", "false")
                    .config("spark.driver.memory", driverMemory)
                    .config("spark.serializer", serializer)
                    .config("spark.kryoserializer.buffer.max", kryoserializerBufferMax)
                    .config("spark.default.parallelism", defaultParallelism)
                    .config("spark.sql.shuffle.partitions", sqlShufflePartitions)
                    // Add Netty-specific configurations
                    .config("spark.driver.extraClassPath", ".")
                    .config("spark.executor.extraClassPath", ".");

            if (distributedLibraries != null && !distributedLibraries.isEmpty()) {
                builder.config("spark.jars", distributedLibraries);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return builder.getOrCreate();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}