package com.tpg.connect.controllers.discovery;

import com.tpg.connect.model.api.PotentialMatchesResponse;
import com.tpg.connect.model.dto.MatchActionsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@Tag(name = "Potential Matches", description = "Discover and interact with potential matches - new sets available at 7pm")
public interface PotentialMatchesControllerApi {

    @Operation(summary = "Get matches status", description = "Check if new matches are available and get status metadata")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Matches status retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/status")
    ResponseEntity<Map<String, Object>> getMatchesStatus(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader
    );

    @Operation(summary = "Get today's matches", description = "Retrieve today's set of potential matches (available after 7pm)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Today's matches retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Matches not yet available - before 7pm"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/today")
    ResponseEntity<PotentialMatchesResponse> getTodaysMatches(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader
    );

    @Operation(summary = "Submit match actions", description = "Submit like/dislike actions for potential matches")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Match actions submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid actions or matches already reviewed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/actions")
    ResponseEntity<Map<String, Object>> submitMatchActions(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "Match actions to submit", required = true)
        @Valid @RequestBody MatchActionsRequest request
    );

    @Operation(summary = "Get matches countdown", description = "Get time remaining until new matches are available (7pm)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Countdown retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/countdown")
    ResponseEntity<Map<String, Object>> getMatchesCountdown(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader
    );

    @Operation(summary = "Get matches history", description = "Retrieve user's match history with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Matches history retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/history")
    ResponseEntity<Map<String, Object>> getMatchesHistory(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "Page number (0-based)")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "10") int size
    );
}