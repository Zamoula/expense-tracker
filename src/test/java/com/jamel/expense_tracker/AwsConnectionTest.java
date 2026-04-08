package com.jamel.expense_tracker;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

@SpringBootTest
public class AwsConnectionTest {
    
    @Autowired
    private DynamoDbClient dynamoDbClient;
    
    @BeforeAll
    static void loadEnv() {
        // Load .env file from project root
        Dotenv dotenv = Dotenv.configure()
                .directory("./")  // Look in project root
                .ignoreIfMissing()
                .load();
        
        // Set each property as a system property
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
            System.out.println("✓ Loaded: " + entry.getKey());
        });
    }
    
    @Test
    public void testAwsConnection() {
        try {
            ListTablesResponse response = dynamoDbClient.listTables();
            System.out.println("\n✅ SUCCESS: Connected to AWS!");
            System.out.println("📋 Tables in your account: " + response.tableNames());
            System.out.println("🎉 Your AWS credentials are working perfectly!\n");
        } catch (Exception e) {
            System.err.println("\n❌ ERROR: Failed to connect to AWS");
            System.err.println("Error message: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
