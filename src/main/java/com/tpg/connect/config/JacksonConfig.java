package com.tpg.connect.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Register JavaTimeModule for LocalDate/LocalDateTime support (essential for our LocalDateTime fix)
        mapper.registerModule(new JavaTimeModule());
        
        // Essential configuration for Java time handling
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Basic configuration for JSON processing
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        
        // Enable standard auto-detection for proper field mapping
        mapper.configure(MapperFeature.AUTO_DETECT_FIELDS, true);
        mapper.configure(MapperFeature.AUTO_DETECT_GETTERS, true);
        mapper.configure(MapperFeature.AUTO_DETECT_SETTERS, true);
        
        // Only serialize non-null values
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        return mapper;
    }
}