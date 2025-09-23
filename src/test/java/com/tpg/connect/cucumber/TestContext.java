package com.tpg.connect.cucumber;

import io.restassured.response.Response;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@Scope("cucumber-glue")
public class TestContext {
    private Response lastResponse;
    private String accessToken;
    private String refreshToken;
    private String verificationToken;
    private String resetToken;
    private String currentUserId;
    private String currentUserEmail;
    private Map<String, Object> testData = new HashMap<>();
    
    public void setTestData(String key, Object value) {
        testData.put(key, value);
    }
    
    public Object getTestData(String key) {
        return testData.get(key);
    }
    
    public void clearContext() {
        lastResponse = null;
        accessToken = null;
        refreshToken = null;
        verificationToken = null;
        resetToken = null;
        currentUserId = null;
        currentUserEmail = null;
        testData.clear();
    }
}