package com.tpg.connect.repository.impl;

import com.tpg.connect.model.MatchSet;
import com.tpg.connect.repository.MatchSetRepository;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class MatchSetRepositoryImpl implements MatchSetRepository {

    private static final String COLLECTION_NAME = "match_sets";
    
    @Autowired
    private Firestore firestore;

    @Override
    public long countByUserId(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .get()
                    .get();
                    
            return (long) querySnapshot.size();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to count match sets by userId", e);
        }
    }

    @Override
    public MatchSet createMatchSet(MatchSet matchSet) {
        try {
            if (matchSet.getCreatedAt() == null) {
                matchSet.setCreatedAt(Timestamp.now());
            }
            if (matchSet.getStatus() == null) {
                matchSet.setStatus("PENDING");
            }
            if (matchSet.getActionsSubmitted() == null) {
                matchSet.setActionsSubmitted(0);
            }
            if (matchSet.getMatchesFound() == null) {
                matchSet.setMatchesFound(0);
            }
            if (matchSet.getViewTime() == null) {
                matchSet.setViewTime(0);
            }
            if (matchSet.getAvgTimePerProfile() == null) {
                matchSet.setAvgTimePerProfile(0);
            }
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(matchSet.getConnectId());
            docRef.set(convertToMap(matchSet)).get();
            
            return matchSet;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to create match set", e);
        }
    }

    @Override
    public Optional<MatchSet> findByConnectId(String connectId) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME)
                    .document(connectId)
                    .get()
                    .get();
                    
            return doc.exists() ? Optional.of(convertToMatchSet(doc)) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find match set by connectId", e);
        }
    }

    @Override
    public List<MatchSet> findByUserId(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToMatchSet)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find match sets by userId", e);
        }
    }

    @Override
    public List<MatchSet> findByUserIdAndDate(String userId, String date) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("date", date)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToMatchSet)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find match sets by userId and date", e);
        }
    }

    @Override
    public List<MatchSet> findActiveMatchSets(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("status", "ACTIVE")
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToMatchSet)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find active match sets", e);
        }
    }

    @Override
    public boolean existsByConnectId(String connectId) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME)
                    .document(connectId)
                    .get()
                    .get();
            return doc.exists();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to check if match set exists", e);
        }
    }

    @Override
    public MatchSet updateMatchSet(MatchSet matchSet) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(matchSet.getConnectId());
            docRef.set(convertToMap(matchSet)).get();
            
            return matchSet;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update match set", e);
        }
    }

    @Override
    public MatchSet updateStatus(String connectId, String status) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", status);
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Match set not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update match set status", e);
        }
    }

    @Override
    public MatchSet markCompleted(String connectId, Timestamp completedAt) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "COMPLETED");
            updates.put("completedAt", completedAt);
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Match set not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to mark match set as completed", e);
        }
    }

    @Override
    public MatchSet updateProgress(String connectId, Integer actionsSubmitted, Integer matchesFound) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            if (actionsSubmitted != null) updates.put("actionsSubmitted", actionsSubmitted);
            if (matchesFound != null) updates.put("matchesFound", matchesFound);
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Match set not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update match set progress", e);
        }
    }

    @Override
    public MatchSet updateViewTime(String connectId, Integer viewTime, Integer avgTimePerProfile) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            if (viewTime != null) updates.put("viewTime", viewTime);
            if (avgTimePerProfile != null) updates.put("avgTimePerProfile", avgTimePerProfile);
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Match set not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update match set view time", e);
        }
    }

    @Override
    public void deleteMatchSet(String connectId) {
        try {
            firestore.collection(COLLECTION_NAME).document(connectId).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete match set", e);
        }
    }

    @Override
    public List<MatchSet> findMatchSetsByDateRange(String userId, String startDate, String endDate) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereGreaterThanOrEqualTo("date", startDate)
                    .whereLessThanOrEqualTo("date", endDate)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToMatchSet)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find match sets by date range", e);
        }
    }

    @Override
    public List<MatchSet> findPendingMatchSets() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("status", "PENDING")
                    .orderBy("createdAt", Query.Direction.ASCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToMatchSet)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find pending match sets", e);
        }
    }

    @Override
    public List<MatchSet> findCompletedMatchSets(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("status", "COMPLETED")
                    .orderBy("completedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToMatchSet)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find completed match sets", e);
        }
    }

    @Override
    public List<MatchSet> findByUserIdOrderByDateDesc(String userId, int offset, int limit) {
        try {
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .orderBy("date", Query.Direction.DESCENDING)
                    .offset(offset)
                    .limit(limit);
                    
            QuerySnapshot querySnapshot = query.get().get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToMatchSet)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find match sets by userId with pagination", e);
        }
    }

    @Override
    public Optional<MatchSet> findById(String id) {
        return findByConnectId(id);
    }

    @Override
    public MatchSet save(MatchSet matchSet) {
        return updateMatchSet(matchSet);
    }

    // Helper conversion methods
    private Map<String, Object> convertToMap(MatchSet matchSet) {
        Map<String, Object> map = new HashMap<>();
        map.put("connectId", matchSet.getConnectId());
        map.put("userId", matchSet.getUserId());
        map.put("date", matchSet.getDate());
        map.put("status", matchSet.getStatus());
        map.put("createdAt", matchSet.getCreatedAt());
        map.put("completedAt", matchSet.getCompletedAt());
        map.put("totalMatches", matchSet.getTotalMatches());
        map.put("actionsSubmitted", matchSet.getActionsSubmitted());
        map.put("matchesFound", matchSet.getMatchesFound());
        map.put("algorithmVersion", matchSet.getAlgorithmVersion());
        map.put("viewTime", matchSet.getViewTime());
        map.put("avgTimePerProfile", matchSet.getAvgTimePerProfile());
        
        if (matchSet.getPotentialMatches() != null) {
            map.put("potentialMatches", matchSet.getPotentialMatches().stream()
                    .map(this::convertPotentialMatchToMap)
                    .collect(Collectors.toList()));
        }
        
        if (matchSet.getFilters() != null) {
            map.put("filters", convertFiltersToMap(matchSet.getFilters()));
        }
        
        return map;
    }

    private MatchSet convertToMatchSet(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) {
            throw new RuntimeException("Document data is null");
        }
        
        return MatchSet.builder()
                .connectId(doc.getId())
                .userId((String) data.get("userId"))
                .date((String) data.get("date"))
                .status((String) data.get("status"))
                .createdAt((Timestamp) data.get("createdAt"))
                .completedAt((Timestamp) data.get("completedAt"))
                .potentialMatches(convertToPotentialMatchList((List<Map<String, Object>>) data.get("potentialMatches")))
                .totalMatches((Integer) data.get("totalMatches"))
                .actionsSubmitted((Integer) data.get("actionsSubmitted"))
                .matchesFound((Integer) data.get("matchesFound"))
                .algorithmVersion((String) data.get("algorithmVersion"))
                .filters(convertToFilters((Map<String, Object>) data.get("filters")))
                .viewTime((Integer) data.get("viewTime"))
                .avgTimePerProfile((Integer) data.get("avgTimePerProfile"))
                .build();
    }

    private Map<String, Object> convertPotentialMatchToMap(MatchSet.PotentialMatch match) {
        Map<String, Object> map = new HashMap<>();
        map.put("connectId", match.getConnectId());
        map.put("name", match.getName());
        map.put("age", match.getAge());
        map.put("location", match.getLocation());
        map.put("interests", match.getInterests());
        map.put("distance", match.getDistance());
        map.put("compatibilityScore", match.getCompatibilityScore());
        map.put("commonInterests", match.getCommonInterests());
        map.put("algorithmReason", match.getAlgorithmReason());
        
        if (match.getPhotos() != null) {
            map.put("photos", match.getPhotos().stream()
                    .map(this::convertPhotoToMap)
                    .collect(Collectors.toList()));
        }
        
        if (match.getProfile() != null) {
            map.put("profile", convertProfileToMap(match.getProfile()));
        }
        
        return map;
    }

    private MatchSet.PotentialMatch convertToPotentialMatch(Map<String, Object> matchMap) {
        if (matchMap == null) return null;
        
        return MatchSet.PotentialMatch.builder()
                .connectId((String) matchMap.get("connectId"))
                .name((String) matchMap.get("name"))
                .age((Integer) matchMap.get("age"))
                .location((String) matchMap.get("location"))
                .interests((List<String>) matchMap.get("interests"))
                .photos(convertToPhotoList((List<Map<String, Object>>) matchMap.get("photos")))
                .profile(convertToProfile((Map<String, Object>) matchMap.get("profile")))
                .distance((Double) matchMap.get("distance"))
                .compatibilityScore((Double) matchMap.get("compatibilityScore"))
                .commonInterests((List<String>) matchMap.get("commonInterests"))
                .algorithmReason((String) matchMap.get("algorithmReason"))
                .build();
    }

    private List<MatchSet.PotentialMatch> convertToPotentialMatchList(List<Map<String, Object>> matchMaps) {
        if (matchMaps == null) return new ArrayList<>();
        
        return matchMaps.stream()
                .map(this::convertToPotentialMatch)
                .collect(Collectors.toList());
    }

    private Map<String, Object> convertPhotoToMap(MatchSet.Photo photo) {
        Map<String, Object> map = new HashMap<>();
        map.put("url", photo.getUrl());
        map.put("isPrimary", photo.getIsPrimary());
        
        if (photo.getPrompts() != null) {
            map.put("prompts", photo.getPrompts().stream()
                    .map(this::convertPhotoPromptToMap)
                    .collect(Collectors.toList()));
        }
        
        return map;
    }

    private MatchSet.Photo convertToPhoto(Map<String, Object> photoMap) {
        if (photoMap == null) return null;
        
        return MatchSet.Photo.builder()
                .url((String) photoMap.get("url"))
                .isPrimary((Boolean) photoMap.get("isPrimary"))
                .prompts(convertToPhotoPromptList((List<Map<String, Object>>) photoMap.get("prompts")))
                .build();
    }

    private List<MatchSet.Photo> convertToPhotoList(List<Map<String, Object>> photoMaps) {
        if (photoMaps == null) return new ArrayList<>();
        
        return photoMaps.stream()
                .map(this::convertToPhoto)
                .collect(Collectors.toList());
    }

    private Map<String, Object> convertPhotoPromptToMap(MatchSet.PhotoPrompt prompt) {
        Map<String, Object> map = new HashMap<>();
        map.put("text", prompt.getText());
        
        if (prompt.getPosition() != null) {
            Map<String, Object> positionMap = new HashMap<>();
            positionMap.put("x", prompt.getPosition().getX());
            positionMap.put("y", prompt.getPosition().getY());
            map.put("position", positionMap);
        }
        
        return map;
    }

    private MatchSet.PhotoPrompt convertToPhotoPrompt(Map<String, Object> promptMap) {
        if (promptMap == null) return null;
        
        MatchSet.Position position = null;
        Map<String, Object> positionMap = (Map<String, Object>) promptMap.get("position");
        if (positionMap != null) {
            position = MatchSet.Position.builder()
                    .x((Double) positionMap.get("x"))
                    .y((Double) positionMap.get("y"))
                    .build();
        }
        
        return MatchSet.PhotoPrompt.builder()
                .text((String) promptMap.get("text"))
                .position(position)
                .build();
    }

    private List<MatchSet.PhotoPrompt> convertToPhotoPromptList(List<Map<String, Object>> promptMaps) {
        if (promptMaps == null) return new ArrayList<>();
        
        return promptMaps.stream()
                .map(this::convertToPhotoPrompt)
                .collect(Collectors.toList());
    }

    private Map<String, Object> convertProfileToMap(MatchSet.Profile profile) {
        if (profile == null) return null;
        
        Map<String, Object> map = new HashMap<>();
        map.put("jobTitle", profile.getJobTitle());
        map.put("datingIntentions", profile.getDatingIntentions());
        return map;
    }

    private MatchSet.Profile convertToProfile(Map<String, Object> profileMap) {
        if (profileMap == null) return null;
        
        return MatchSet.Profile.builder()
                .jobTitle((String) profileMap.get("jobTitle"))
                .datingIntentions((String) profileMap.get("datingIntentions"))
                .build();
    }

    private Map<String, Object> convertFiltersToMap(MatchSet.Filters filters) {
        if (filters == null) return null;
        
        Map<String, Object> map = new HashMap<>();
        map.put("ageRange", filters.getAgeRange());
        map.put("maxDistance", filters.getMaxDistance());
        map.put("preferredGender", filters.getPreferredGender());
        map.put("otherPreferences", filters.getOtherPreferences());
        return map;
    }

    private MatchSet.Filters convertToFilters(Map<String, Object> filtersMap) {
        if (filtersMap == null) return null;
        
        return MatchSet.Filters.builder()
                .ageRange((List<Integer>) filtersMap.get("ageRange"))
                .maxDistance((Integer) filtersMap.get("maxDistance"))
                .preferredGender((String) filtersMap.get("preferredGender"))
                .otherPreferences((Map<String, Object>) filtersMap.get("otherPreferences"))
                .build();
    }
}