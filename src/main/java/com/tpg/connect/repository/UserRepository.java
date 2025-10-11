package com.tpg.connect.repository;

import com.tpg.connect.model.User;
import com.tpg.connect.model.user.ApplicationStatus;
import com.tpg.connect.model.user.UserStatus;
import com.google.cloud.Timestamp;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserRepository {
    
    // Create Operations
    User createUser(User user);
    User createUserWithConnectId(User user, String connectId);
    
    // Read Operations
    Optional<User> findByConnectId(String connectId);
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    List<User> findActiveUsers();
    boolean existsByConnectId(String connectId);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    
    // Update Operations
    User updateUser(User user);
    User save(User user);
    User updateEmailVerificationStatus(String connectId, boolean verified, Timestamp verifiedAt);
    User updateLastLogin(String connectId, Timestamp loginTime, String deviceType);
    User addFcmToken(String connectId, User.FcmToken token);
    User removeFcmToken(String connectId, String deviceId);
    User updateFcmTokenLastUsed(String connectId, String deviceId, Timestamp lastUsed);
    
    // Delete Operations
    void softDeleteUser(String connectId);
    void hardDeleteUser(String connectId);
    
    // Batch Operations
    List<User> findUsersByConnectIds(List<String> connectIds);
    Map<String, User> findUserMapByConnectIds(List<String> connectIds);
    
    // Admin Operations
    List<User> findAllForAdmin(int page, int size, String search, String status, String sortBy, String sortDirection);
    long countForAdmin(String search, String status);
    
    // Status-based queries
    List<User> findByApplicationStatus(ApplicationStatus applicationStatus);
    List<User> findByUserStatus(UserStatus userStatus);
    long countByApplicationStatus(ApplicationStatus applicationStatus);
    long countByUserStatus(UserStatus userStatus);
}