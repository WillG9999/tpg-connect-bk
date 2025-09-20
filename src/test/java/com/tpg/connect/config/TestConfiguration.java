package com.tpg.connect.config;

import com.tpg.connect.client.database.*;
import com.tpg.connect.model.user.auth.User;
import com.tpg.connect.model.user.CompleteUserProfile;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfiguration {

    @Bean
    @Primary
    public UserRepository mockUserRepository() {
        UserRepository mockRepo = Mockito.mock(UserRepository.class);
        
        // Create test user
        User testUser = new User();
        testUser.setId("test_user_123");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("$2a$10$encoded_password_hash");
        testUser.setRole("USER");
        testUser.setActive(true);
        testUser.setEmailVerified(true);
        testUser.setCreatedAt(LocalDateTime.now());
        
        // Mock repository methods
        when(mockRepo.findByEmail("test@example.com")).thenReturn(testUser);
        when(mockRepo.findById("test_user_123")).thenReturn(testUser);
        when(mockRepo.findByUsername("testuser")).thenReturn(testUser);
        when(mockRepo.existsByUsername("testuser")).thenReturn(true);
        when(mockRepo.existsByUsername("newuser")).thenReturn(false);
        when(mockRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        return mockRepo;
    }

    @Bean
    @Primary
    public UserProfileRepository mockUserProfileRepository() {
        UserProfileRepository mockRepo = Mockito.mock(UserProfileRepository.class);
        
        // Create test profile
        CompleteUserProfile testProfile = new CompleteUserProfile();
        testProfile.setId("test_user_123");
        testProfile.setUserId("test_user_123");
        testProfile.setName("Test User");
        testProfile.setFirstName("Test");
        testProfile.setLastName("User");
        testProfile.setAge(28);
        testProfile.setBio("Test user profile");
        testProfile.setLocation("Test City, TS");
        testProfile.setActive(true);
        testProfile.setCreatedAt(LocalDateTime.now());
        testProfile.setUpdatedAt(LocalDateTime.now());
        testProfile.setPhotos(new ArrayList<>());
        testProfile.setInterests(List.of("Testing", "Development"));
        
        // Mock repository methods
        when(mockRepo.findByUserId("test_user_123")).thenReturn(testProfile);
        when(mockRepo.existsByUserId("test_user_123")).thenReturn(true);
        when(mockRepo.save(any(CompleteUserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mockRepo.findAll()).thenReturn(List.of(testProfile));
        
        return mockRepo;
    }

    @Bean
    @Primary
    public MatchRepository mockMatchRepository() {
        return Mockito.mock(MatchRepository.class);
    }

    @Bean
    @Primary
    public SafetyBlockRepository mockSafetyBlockRepository() {
        return Mockito.mock(SafetyBlockRepository.class);
    }

    @Bean
    @Primary
    public UserReportRepository mockUserReportRepository() {
        return Mockito.mock(UserReportRepository.class);
    }

    @Bean
    @Primary
    public MatchSetRepository mockMatchSetRepository() {
        return Mockito.mock(MatchSetRepository.class);
    }

    @Bean
    @Primary
    public BlockedUserRepository mockBlockedUserRepository() {
        return Mockito.mock(BlockedUserRepository.class);
    }

    @Bean
    @Primary
    public UserActionRepository mockUserActionRepository() {
        return Mockito.mock(UserActionRepository.class);
    }

    @Bean
    @Primary
    public SubscriptionRepository mockSubscriptionRepository() {
        return Mockito.mock(SubscriptionRepository.class);
    }

    @Bean
    @Primary
    public NotificationRepository mockNotificationRepository() {
        return Mockito.mock(NotificationRepository.class);
    }

    @Bean
    @Primary
    public DeviceTokenRepository mockDeviceTokenRepository() {
        return Mockito.mock(DeviceTokenRepository.class);
    }
}