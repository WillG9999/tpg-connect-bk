package com.tpg.connect.repository;

import com.tpg.connect.model.notifications.DeviceToken;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository {
    
    // Create Operations
    DeviceToken save(DeviceToken deviceToken);
    List<DeviceToken> saveAll(List<DeviceToken> deviceTokens);
    
    // Read Operations
    Optional<DeviceToken> findById(String id);
    Optional<DeviceToken> findByToken(String token);
    Optional<DeviceToken> findByUserIdAndDeviceId(String userId, String deviceId);
    List<DeviceToken> findByUserId(String userId);
    List<DeviceToken> findByUserIdAndActive(String userId, boolean active);
    List<DeviceToken> findActiveTokensByUserId(String userId);
    List<DeviceToken> findByDeviceType(String deviceType);
    List<DeviceToken> findInactiveTokens(LocalDateTime cutoffDate);
    List<DeviceToken> findExpiredTokens(LocalDateTime cutoffDate);
    
    // Update Operations
    DeviceToken updateLastUsed(String id);
    DeviceToken setActive(String id, boolean active);
    
    // Delete Operations
    void deleteById(String id);
    void deleteByUserId(String userId);
    void deleteByToken(String token);
    void deleteByUserIdAndDeviceId(String userId, String deviceId);
    void deleteAll(List<DeviceToken> deviceTokens);
    void deleteInactiveTokens(int daysInactive);
}