package com.tpg.connect.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);
    private final ObjectMapper objectMapper;

    @Autowired
    public WebMvcConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.info("Configuring CORS mappings");
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:3001", "http://127.0.0.1:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
        logger.info("CORS configuration completed - allowing all origins for development");
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        logger.info("Extending message converters with custom ObjectMapper");
        logger.info("Initial converter count: {}", converters.size());
        
        // Remove existing Jackson converters and replace with our custom one
        converters.removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);
        
        // Create and configure Jackson converter with our custom ObjectMapper
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        jacksonConverter.setObjectMapper(objectMapper);
        
        // Add our converter first for priority
        converters.add(0, jacksonConverter);
        logger.info("Added custom MappingJackson2HttpMessageConverter with our ObjectMapper");
        logger.info("Final converter count: {}", converters.size());
    }
}