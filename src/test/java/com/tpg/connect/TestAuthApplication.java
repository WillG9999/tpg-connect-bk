package com.tpg.connect;

import com.tpg.connect.config.TestConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@Import(TestConfiguration.class)
public class TestAuthApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "test");
        SpringApplication.run(TestAuthApplication.class, args);
    }
}