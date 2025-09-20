package com.tpg.connect.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public abstract class BaseController {

    protected ResponseEntity<Map<String, Object>> successResponse(Object data) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", data,
                "statusCode", 200
        ));
    }

    protected ResponseEntity<Map<String, Object>> successResponse(Object data, String message) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", data,
                "message", message,
                "statusCode", 200
        ));
    }

    protected ResponseEntity<Map<String, Object>> errorResponse(String error) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "error", error,
                "statusCode", 400
        ));
    }

    protected ResponseEntity<Map<String, Object>> errorResponse(String error, HttpStatus status) {
        return ResponseEntity.status(status).body(Map.of(
                "success", false,
                "error", error,
                "statusCode", status.value()
        ));
    }

    protected ResponseEntity<Map<String, Object>> unauthorizedResponse(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "error", message,
                "statusCode", 401
        ));
    }

    protected ResponseEntity<Map<String, Object>> notFoundResponse(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "error", message,
                "statusCode", 404
        ));
    }
}