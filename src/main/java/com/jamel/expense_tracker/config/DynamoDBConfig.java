package com.jamel.expense_tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DynamoDBConfig {

    @Bean
    public DynamoDbClient dynamoDbClient() {
        // Try to load from .env file first
        Map<String, String> env = loadEnvFile();
        
        // Read from .env or system environment
        String accessKeyId = env.getOrDefault("AWS_ACCESS_KEY_ID", System.getenv("AWS_ACCESS_KEY_ID"));
        String secretAccessKey = env.getOrDefault("AWS_SECRET_ACCESS_KEY", System.getenv("AWS_SECRET_ACCESS_KEY"));
        String region = env.getOrDefault("AWS_DEFAULT_REGION", System.getenv("AWS_DEFAULT_REGION"));
        
        if (accessKeyId == null || accessKeyId.isEmpty() || 
            secretAccessKey == null || secretAccessKey.isEmpty()) {
            System.err.println("❌ AWS credentials not found!");
            System.err.println("Please check your .env file or environment variables");
            throw new IllegalStateException("AWS credentials not found");
        }
        
        System.out.println("✓ AWS credentials loaded successfully");
        System.out.println("  Region: " + (region != null ? region : "us-east-1"));
        
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        
        return DynamoDbClient.builder()
                .region(Region.of(region != null ? region : "us-east-1"))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    private Map<String, String> loadEnvFile() {
        Map<String, String> env = new HashMap<>();
        Path envPath = Paths.get(".env");
        
        if (Files.exists(envPath)) {
            try {
                Files.lines(envPath).forEach(line -> {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                        String[] parts = line.split("=", 2);
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        env.put(key, value);
                        System.out.println("  Loaded from .env: " + key);
                    }
                });
            } catch (IOException e) {
                System.err.println("Warning: Could not read .env file: " + e.getMessage());
            }
        } else {
            System.out.println("No .env file found, using system environment variables");
        }
        
        return env;
    }

    @Bean
    public boolean createTableIfNotExists(DynamoDbClient dynamoDbClient) {
        // Load table name from .env or use default
        Map<String, String> env = loadEnvFile();
        String tableName = env.getOrDefault("AWS_DYNAMODB_TABLE_NAME", 
                           System.getenv().getOrDefault("AWS_DYNAMODB_TABLE_NAME", "expenses"));
        
        try {
            // Check if table exists
            dynamoDbClient.describeTable(DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build());
            System.out.println("✓ Table '" + tableName + "' already exists");
            return true;
            
        } catch (DynamoDbException e) {
            System.out.println("Creating table: " + tableName);
            
            CreateTableRequest request = CreateTableRequest.builder()
                    .tableName(tableName)
                    .keySchema(
                        KeySchemaElement.builder()
                            .attributeName("userId")
                            .keyType(KeyType.HASH)
                            .build(),
                        KeySchemaElement.builder()
                            .attributeName("expenseId")
                            .keyType(KeyType.RANGE)
                            .build()
                    )
                    .attributeDefinitions(
                        AttributeDefinition.builder()
                            .attributeName("userId")
                            .attributeType(ScalarAttributeType.S)
                            .build(),
                        AttributeDefinition.builder()
                            .attributeName("expenseId")
                            .attributeType(ScalarAttributeType.S)
                            .build()
                    )
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build();
            
            dynamoDbClient.createTable(request);
            System.out.println("✓ Table creation initiated for: " + tableName);
            return true;
        }
    }
}