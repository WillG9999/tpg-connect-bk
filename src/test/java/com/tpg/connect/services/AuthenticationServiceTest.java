package com.tpg.connect.services;

import com.tpg.connect.client.database.UserRepository;
import com.tpg.connect.client.database.UserProfileRepository;
import com.tpg.connect.model.api.LoginResponse;
import com.tpg.connect.model.api.RegisterResponse;
import com.tpg.connect.model.dto.LoginRequest;
import com.tpg.connect.model.dto.RegisterRequest;
import com.tpg.connect.model.user.auth.User;
import com.tpg.connect.model.user.CompleteUserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private ProfileManagementService profileManagementService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        // We can't easily mock the private passwordEncoder, so we'll work around it
    }

    @Test
    void testRegisterUser_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        request.setDateOfBirth(LocalDate.of(1995, 1, 1));
        request.setGender("Male");
        request.setLocation("Test City");

        when(userRepository.findByEmail("john@example.com")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userProfileRepository.save(any(CompleteUserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendEmailVerification(anyString(), anyString(), anyString());

        // Act
        RegisterResponse response = authenticationService.registerUser(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Registration successful. Please verify your email.", response.getMessage());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getUser());
        assertEquals("John Doe", response.getUser().getName());

        verify(userRepository).findByEmail("john@example.com");
        verify(userRepository).save(any(User.class));
        verify(userProfileRepository).save(any(CompleteUserProfile.class));
        verify(emailService).sendEmailVerification(anyString(), anyString(), anyString());
    }

    @Test
    void testRegisterUser_PasswordMismatch() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setPassword("password123");
        request.setConfirmPassword("different_password");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authenticationService.registerUser(request)
        );
        assertEquals("Passwords do not match", exception.getMessage());
    }

    @Test
    void testRegisterUser_UserTooYoung() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        request.setDateOfBirth(LocalDate.of(2010, 1, 1)); // Too young

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authenticationService.registerUser(request)
        );
        assertEquals("User must be at least 18 years old", exception.getMessage());
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        request.setDateOfBirth(LocalDate.of(1995, 1, 1));

        User existingUser = new User();
        existingUser.setEmail("existing@example.com");
        when(userRepository.findByEmail("existing@example.com")).thenReturn(existingUser);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authenticationService.registerUser(request)
        );
        assertEquals("Email already registered", exception.getMessage());
    }

    @Test
    void testLoginUser_Success() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setId("user_123");
        user.setEmail("test@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setActive(true);

        CompleteUserProfile profile = new CompleteUserProfile();
        profile.setUserId("user_123");
        profile.setName("Test User");
        profile.setAge(28);

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userProfileRepository.findByUserId("user_123")).thenReturn(profile);

        // Act
        LoginResponse response = authenticationService.loginUser(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals("Test User", response.getUser().getName());

        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(userProfileRepository).findByUserId("user_123");
    }

    @Test
    void testLoginUser_InvalidEmail() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authenticationService.loginUser(request)
        );
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testLoginUser_InvalidPassword() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong_password");

        User user = new User();
        user.setId("user_123");
        user.setEmail("test@example.com");
        user.setPasswordHash(passwordEncoder.encode("correct_password"));
        user.setActive(true);

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authenticationService.loginUser(request)
        );
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testLoginUser_InactiveAccount() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setId("user_123");
        user.setEmail("test@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setActive(false); // Inactive account

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authenticationService.loginUser(request)
        );
        assertEquals("Account is deactivated", exception.getMessage());
    }
}