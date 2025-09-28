package com.tpg.connect.services;

import com.tpg.connect.repository.MatchSetRepository;
import com.tpg.connect.repository.UserActionRepository;
import com.tpg.connect.repository.UserProfileRepository;
import com.tpg.connect.model.api.PotentialMatchesResponse;
import com.tpg.connect.model.MatchSet;
import com.tpg.connect.model.dto.MatchActionsRequest;
import com.tpg.connect.model.match.UserAction;
import com.tpg.connect.model.user.CompleteUserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PotentialMatchesService {

    @Autowired
    private MatchSetRepository matchSetRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserActionRepository userActionRepository;

    @Autowired
    private MatchService matchService;


    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserActionsService userActionsService;

    private static final int MATCH_SET_SIZE = 10;
    private static final LocalTime MATCHES_RELEASE_TIME = LocalTime.of(19, 0); // 7:00 PM

    public Map<String, Object> getMatchesStatus(String userId) {
        LocalDate today = LocalDate.now();
        String matchSetId = generateMatchSetId(userId, today);
        
        MatchSet matchSet = matchSetRepository.findById(matchSetId).orElse(null);
        boolean matchSetExists = matchSet != null;
        boolean matchesAvailable = LocalTime.now().isAfter(MATCHES_RELEASE_TIME);
        boolean matchSetCompleted = matchSetExists && "COMPLETED".equals(matchSet.getStatus());
        
        Map<String, Object> status = new HashMap<>();
        status.put("matchSetId", matchSetId);
        status.put("date", today.toString());
        status.put("available", matchesAvailable);
        status.put("completed", matchSetCompleted);
        status.put("releaseTime", MATCHES_RELEASE_TIME.toString());
        status.put("currentTime", LocalTime.now().toString());
        
        if (matchSetExists) {
            status.put("totalUsers", matchSet.getTotalMatches());
            status.put("actionsSubmitted", matchSet.getActionsSubmitted());
            status.put("matches", matchSet.getMatchesFound());
        }
        
        return status;
    }

    @Cacheable(value = "potentialMatches", key = "'matches_' + #userId + '_' + T(java.time.LocalDate).now()")
    public PotentialMatchesResponse getTodaysMatches(String userId) {
        LocalDate today = LocalDate.now();
        String matchSetId = generateMatchSetId(userId, today);
        
        // Check if match set already exists
        MatchSet existingMatchSet = matchSetRepository.findById(matchSetId).orElse(null);
        if (existingMatchSet != null) {
            List<CompleteUserProfile> users = getUserProfilesFromMatchSet(existingMatchSet);
            return new PotentialMatchesResponse(true, "Today's matches retrieved", users, matchSetId, 
                    existingMatchSet.getTotalMatches(), "COMPLETED".equals(existingMatchSet.getStatus()));
        }
        
        // Generate new match set
        MatchSet matchSet = generateMatchSetForUser(userId, today);
        List<CompleteUserProfile> users = getUserProfilesFromMatchSet(matchSet);
        
        // Send notification about new potential matches
        if (!users.isEmpty()) {
            notificationService.sendPotentialMatchesReadyNotification(userId, users.size());
        }
        
        return new PotentialMatchesResponse(true, "Today's matches generated", users, matchSetId, 
                users.size(), false);
    }

    public Map<String, Object> submitMatchActions(String userId, MatchActionsRequest request) {
        MatchSet matchSet = matchSetRepository.findById(request.getMatchSetId()).orElse(null);
        if (matchSet == null) {
            throw new IllegalArgumentException("Match set not found: " + request.getMatchSetId());
        }
        
        if (!matchSet.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Match set does not belong to user");
        }
        
        if ("COMPLETED".equals(matchSet.getStatus())) {
            throw new IllegalStateException("Match set already completed");
        }
        
        int matches = 0;
        List<String> newMatches = new ArrayList<>();
        
        // Process each action
        for (MatchActionsRequest.UserActionDto actionDto : request.getActions()) {
            UserAction.ActionType action = "LIKE".equals(actionDto.getAction()) ? 
                    UserAction.ActionType.LIKE : UserAction.ActionType.PASS;
            
            // Record the action
            matchService.recordUserAction(userId, actionDto.getTargetUserId(), action);
            
            // Check for match if it's a like
            if (action == UserAction.ActionType.LIKE) {
                if (matchService.checkForMutualLike(userId, actionDto.getTargetUserId())) {
                    matchService.createMatch(userId, actionDto.getTargetUserId());
                    matches++;
                    newMatches.add(actionDto.getTargetUserId());
                }
            }
        }
        
        // Update match set status
        matchSet.setStatus("COMPLETED");
        matchSet.setActionsSubmitted(request.getActions().size());
        matchSet.setMatchesFound(matches);
        matchSet.setCompletedAt(com.google.cloud.Timestamp.now());
        
        matchSetRepository.save(matchSet);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("actionsProcessed", request.getActions().size());
        result.put("matchesFound", matches);
        result.put("newMatches", newMatches);
        result.put("matchSetCompleted", true);
        
        return result;
    }

    public Map<String, Object> getMatchesHistory(String userId, int page, int size) {
        List<MatchSet> matchSets = matchSetRepository.findByUserIdOrderByDateDesc(userId, page, size);
        
        List<Map<String, Object>> matchSetSummaries = matchSets.stream()
                .map(this::createMatchSetSummary)
                .collect(Collectors.toList());
        
        long totalMatchSets = matchSetRepository.countByUserId(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("matchSets", matchSetSummaries);
        result.put("page", page);
        result.put("size", size);
        result.put("totalElements", totalMatchSets);
        result.put("totalPages", (totalMatchSets + size - 1) / size);
        
        return result;
    }

    public String getCurrentMatchSetId(String userId) {
        LocalDate today = LocalDate.now();
        return generateMatchSetId(userId, today);
    }

    private MatchSet generateMatchSetForUser(String userId, LocalDate date) {
        CompleteUserProfile user = userProfileRepository.findByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        
        // Get users that haven't been acted on recently
        Set<String> recentlyActedOn = getRecentlyActedOnUsers(userId, 30); // Last 30 days
        Set<String> blockedUsers = getBlockedUsers(userId);
        
        // Get potential matches
        List<CompleteUserProfile> candidates = userProfileRepository.findAll().stream()
                .filter(candidate -> !candidate.getUserId().equals(userId)) // Not self
                .filter(candidate -> !recentlyActedOn.contains(candidate.getUserId())) // Not recently acted on
                .filter(candidate -> !blockedUsers.contains(candidate.getUserId())) // Not blocked
                .filter(candidate -> meetsBasicCriteria(user, candidate)) // Basic compatibility
                .limit(MATCH_SET_SIZE)
                .collect(Collectors.toList());
        
        // Create match set
        List<MatchSet.PotentialMatch> potentialMatches = candidates.stream().map(candidate -> 
                MatchSet.PotentialMatch.builder()
                        .connectId(candidate.getUserId())
                        .name(candidate.getName())
                        .age(candidate.getAge())
                        .location(candidate.getLocation())
                        .interests(candidate.getInterests())
                        .build()).collect(Collectors.toList());
        
        MatchSet matchSet = MatchSet.builder()
                .connectId(generateMatchSetId(userId, date))
                .userId(userId)
                .date(date.toString())
                .status("PENDING")
                .createdAt(com.google.cloud.Timestamp.now())
                .actionsSubmitted(0)
                .matchesFound(0)
                .totalMatches(candidates.size())
                .potentialMatches(potentialMatches)
                .build();
        
        return matchSetRepository.save(matchSet);
    }

    private List<CompleteUserProfile> getUserProfilesFromMatchSet(MatchSet matchSet) {
        // For base MatchSet model, we need to extract user IDs from PotentialMatches
        if (matchSet.getPotentialMatches() != null) {
            return matchSet.getPotentialMatches().stream()
                    .map(pm -> userProfileRepository.findByUserId(pm.getConnectId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private Set<String> getRecentlyActedOnUsers(String userId, int days) {
        // Use new UserActionsService for efficient filtering (no need for time-based filtering anymore)
        // Since the userActions collection contains ALL actions, we filter by all acted-on users
        return userActionsService.getActedOnUsers(userId);
    }

    private Set<String> getBlockedUsers(String userId) {
        // In real implementation, would query blocked users from database
        return new HashSet<>(); // Placeholder
    }

    private boolean meetsBasicCriteria(CompleteUserProfile user, CompleteUserProfile candidate) {
        // Basic age compatibility
        if (user.getPreferences() != null) {
            int candidateAge = candidate.getAge();
            return candidateAge >= user.getPreferences().getMinAge() && 
                   candidateAge <= user.getPreferences().getMaxAge();
        }
        return true; // Default to true if no preferences set
    }

    private String generateMatchSetId(String userId, LocalDate date) {
        return "matchset_" + userId + "_" + date.toString();
    }

    private Map<String, Object> createMatchSetSummary(MatchSet matchSet) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", matchSet.getConnectId());
        summary.put("date", matchSet.getDate());
        summary.put("status", matchSet.getStatus());
        summary.put("totalUsers", matchSet.getTotalMatches());
        summary.put("actionsSubmitted", matchSet.getActionsSubmitted());
        summary.put("matchesFound", matchSet.getMatchesFound());
        summary.put("createdAt", matchSet.getCreatedAt());
        summary.put("completedAt", matchSet.getCompletedAt());
        return summary;
    }
}