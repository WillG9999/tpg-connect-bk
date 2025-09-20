package com.tpg.connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ConnectApplication {

	private static final Logger logger = LoggerFactory.getLogger(ConnectApplication.class);

	public static void main(String[] args) {
		// Set default profile to 'dev' if no profile is specified
		if (System.getProperty("spring.profiles.active") == null && 
			System.getenv("SPRING_PROFILES_ACTIVE") == null) {
			System.setProperty("spring.profiles.active", "dev");
			logger.info("No active profile set, defaulting to 'dev' profile");
		}

		ConfigurableApplicationContext context = SpringApplication.run(ConnectApplication.class, args);
		Environment env = context.getEnvironment();
		
		String[] activeProfiles = env.getActiveProfiles();
		String port = env.getProperty("server.port", "8080");
		String contextPath = env.getProperty("server.servlet.context-path", "");
		
		logger.info("");
		logger.info("=".repeat(60));
		logger.info("🚀 Connect Application Started Successfully!");
		logger.info("=".repeat(60));
		logger.info("📋 Active Profiles: {}", String.join(", ", activeProfiles));
		logger.info("🌐 Server URL: http://localhost:{}{}", port, contextPath);
		logger.info("📖 API Documentation: http://localhost:{}{}/swagger-ui.html", port, contextPath);
		logger.info("🔧 Actuator Health: http://localhost:{}{}/actuator/health", port, contextPath);
		
		// Log Firebase configuration status
		String firebaseConfigPath = env.getProperty("firebase.config.path");
		String firebaseProjectId = env.getProperty("firebase.project.id");
		String storageBucket = env.getProperty("firebase.storage.bucket");
		
		logger.info("🔥 Firebase Configuration:");
		logger.info("   Project ID: {}", firebaseProjectId != null ? firebaseProjectId : "Not configured");
		logger.info("   Storage Bucket: {}", storageBucket != null ? storageBucket : "Not configured");
		logger.info("   Config Path: {}", firebaseConfigPath != null ? firebaseConfigPath : "Not configured");
		
		// Development mode specific logging
		if (java.util.Arrays.asList(activeProfiles).contains("dev")) {
			logger.info("🛠️  Development Mode Features:");
			logger.info("   - Enhanced logging enabled");
			logger.info("   - CORS configured for local development");
			logger.info("   - Mock services available");
			logger.info("   - Test data creation enabled");
		}
		
		logger.info("=".repeat(60));
		logger.info("");
		
		// Log startup warnings if Firebase is not configured
		if (firebaseConfigPath == null || firebaseProjectId == null) {
			logger.warn("⚠️  Firebase is not fully configured!");
			logger.warn("   Please ensure you have:");
			logger.warn("   1. Created a Firebase project");
			logger.warn("   2. Downloaded the service account key");
			logger.warn("   3. Placed it in the configured path");
			logger.warn("   See: src/main/resources/firebase/README.md for setup instructions");
		}
	}
}
