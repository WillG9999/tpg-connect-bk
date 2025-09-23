package com.tpg.connect.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Cucumber configuration using REAL database connections
 * This will create actual users and data in your Firebase/Firestore database
 */
@CucumberContextConfiguration
@SpringBootTest(classes = com.tpg.connect.ConnectApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CucumberTestConfiguration {
    
    @LocalServerPort
    private int port;
    
    public int getPort() {
        return port;
    }
}