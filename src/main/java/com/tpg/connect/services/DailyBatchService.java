package com.tpg.connect.services;

import org.springframework.stereotype.Service;

@Service
public class DailyBatchService {
    
    public String getCurrentBatchId(String userId) {
        // Mock implementation - return a timestamp-based batch ID
        return "batch_" + userId + "_" + System.currentTimeMillis();
    }
}