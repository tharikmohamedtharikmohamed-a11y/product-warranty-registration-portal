package com.warrantyportal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Configuration
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    public CommandLineRunner verifyDatabaseConnection(DataSource dataSource) {
        return args -> {
            logger.info("Verifying database connection to Supabase PostgreSQL...");
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                logger.info("Successfully connected to Database!");
                logger.info("Database Product Name: {}", metaData.getDatabaseProductName());
                logger.info("Database Product Version: {}", metaData.getDatabaseProductVersion());
                logger.info("Driver Name: {}", metaData.getDriverName());
                logger.info("URL: {}", metaData.getURL());
            } catch (Exception e) {
                logger.error("Failed to connect to Supabase PostgreSQL database: {}", e.getMessage(), e);
            }
        };
    }
}
