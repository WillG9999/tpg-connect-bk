package com.tpg.connect.repository;

import com.tpg.connect.model.UserBlocked;

public interface UserBlockedRepository {
    
    /**
     * Find user's blocking configuration by connect ID
     */
    UserBlocked findByConnectId(String connectId);
    
    /**
     * Save or update user's blocking configuration
     */
    UserBlocked save(UserBlocked userBlocked);
    
    /**
     * Delete user's blocking configuration
     */
    void delete(String connectId);
    
    /**
     * Check if user blocking configuration exists
     */
    boolean existsByConnectId(String connectId);
    
    /**
     * Create a new empty user blocking configuration
     */
    UserBlocked createEmptyUserBlocked(String connectId);
}