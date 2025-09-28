package com.tpg.connect.controllers.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tpg.connect.controllers.BaseController;
import com.tpg.connect.services.AdminStatsService;

import java.util.Map;

/**
 * REST controller for admin statistics and analytics
 */
@RestController
@RequestMapping("/api/admin/stats")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class AdminStatsController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AdminStatsController.class);

    @Autowired
    private AdminStatsService adminStatsService;

    /**
     * Get demographics statistics (gender and interest distribution)
     */
    @GetMapping("/demographics")
    public ResponseEntity<Map<String, Object>> getDemographicsStats() {
        logger.info("📊 Admin requesting demographics statistics");
        
        try {
            Map<String, Object> stats = adminStatsService.getDemographicsStatistics();
            logger.info("✅ Successfully retrieved demographics stats");
            
            return successResponse(stats, "Demographics statistics retrieved successfully");
            
        } catch (Exception e) {
            logger.error("❌ Error retrieving demographics statistics: {}", e.getMessage(), e);
            return errorResponse("Failed to retrieve demographics statistics: " + e.getMessage());
        }
    }
}