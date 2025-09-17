package com.tpg.connect.controllers.user;

import com.tpg.connect.constants.enums.EndpointConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Tag(name = "User Profile", description = "User profile management endpoints")
@RequestMapping(EndpointConstants.Users.BASE)
@SecurityRequirement(name = "Bearer Authentication")
public interface UserControllerApi {

    @Operation(
        summary = "Get current user profile", 
        description = "Retrieves the complete profile of the currently authenticated user. Supports cache-first behavior with 24-hour cache duration."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "404", description = "User profile not found")
    })
    @GetMapping("/me")
    ResponseEntity<Map<String, Object>> getCurrentUserProfile(
        @Parameter(description = "Authorization header with Bearer token", required = true)
        @RequestHeader(EndpointConstants.Headers.AUTHORIZATION) String authHeader,
        
        @Parameter(description = "Include user preferences in response", required = false)
        @RequestParam(value = EndpointConstants.QueryParams.INCLUDE_PREFERENCES, defaultValue = "false") boolean includePreferences
    );
}