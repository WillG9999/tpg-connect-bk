package com.tpg.connect.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@Profile("dev")
public class DevConfig {

    private static final Logger logger = LoggerFactory.getLogger(DevConfig.class);

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3001,http://127.0.0.1:3000}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    @Bean
    @Profile("dev")
    public CorsConfigurationSource corsConfigurationSource() {
        logger.info("Configuring CORS for development environment");
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        logger.info("CORS allowed origins: {}", origins);
        
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);
        logger.info("CORS allowed methods: {}", methods);
        
        if ("*".equals(allowedHeaders)) {
            configuration.addAllowedHeader("*");
        } else {
            List<String> headers = Arrays.asList(allowedHeaders.split(","));
            configuration.setAllowedHeaders(headers);
        }
        logger.info("CORS allowed headers: {}", allowedHeaders);
        
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        logger.info("CORS configuration completed for development");
        return source;
    }

    @Bean
    @Profile("dev")
    public DevProperties devProperties() {
        DevProperties properties = new DevProperties();
        properties.setMockEmailService(false);
        properties.setCreateSampleData(true);
        properties.setDetailedErrorResponses(true);
        properties.setAutoCreateTestUsers(true);
        
        logger.info("Development properties configured:");
        logger.info("  Mock email service: {}", properties.isMockEmailService());
        logger.info("  Create sample data: {}", properties.isCreateSampleData());
        logger.info("  Detailed error responses: {}", properties.isDetailedErrorResponses());
        logger.info("  Auto create test users: {}", properties.isAutoCreateTestUsers());
        
        return properties;
    }

    public static class DevProperties {
        private boolean mockEmailService;
        private boolean createSampleData;
        private boolean detailedErrorResponses;
        private boolean autoCreateTestUsers;

        // Getters and setters
        public boolean isMockEmailService() {
            return mockEmailService;
        }

        public void setMockEmailService(boolean mockEmailService) {
            this.mockEmailService = mockEmailService;
        }

        public boolean isCreateSampleData() {
            return createSampleData;
        }

        public void setCreateSampleData(boolean createSampleData) {
            this.createSampleData = createSampleData;
        }

        public boolean isDetailedErrorResponses() {
            return detailedErrorResponses;
        }

        public void setDetailedErrorResponses(boolean detailedErrorResponses) {
            this.detailedErrorResponses = detailedErrorResponses;
        }

        public boolean isAutoCreateTestUsers() {
            return autoCreateTestUsers;
        }

        public void setAutoCreateTestUsers(boolean autoCreateTestUsers) {
            this.autoCreateTestUsers = autoCreateTestUsers;
        }
    }
}