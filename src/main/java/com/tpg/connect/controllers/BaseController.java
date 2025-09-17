package com.tpg.connect.controllers;

import org.springframework.http.ResponseEntity;

import java.util.Map;

public abstract class BaseController {

    protected ResponseEntity<Map<String, Object>> successResponse(Object data) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Operation completed successfully",
                "data", data
        ));
    }
}