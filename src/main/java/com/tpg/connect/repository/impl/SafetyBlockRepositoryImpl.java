package com.tpg.connect.repository.impl;

import com.tpg.connect.model.SafetyBlock;
import com.tpg.connect.repository.SafetyBlockRepository;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class SafetyBlockRepositoryImpl implements SafetyBlockRepository {

    private static final String COLLECTION_NAME = "safety_blocks";
    
    @Autowired
    private Firestore firestore;

    @Override
    public void delete(SafetyBlock safetyBlock) {
        try {
            firestore.collection(COLLECTION_NAME).document(safetyBlock.getConnectId()).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete safety block", e);
        }
    }

    @Override
    public List<SafetyBlock> findByUserIdAndEnabled(String userId, boolean enabled) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("enabled", enabled)
                    .orderBy("blockedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToSafetyBlock)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find safety blocks by userId and enabled", e);
        }
    }

    @Override
    public SafetyBlock createSafetyBlock(SafetyBlock safetyBlock) {
        try {
            if (safetyBlock.getBlockedAt() == null) {
                safetyBlock.setBlockedAt(Timestamp.now());
            }
            if (safetyBlock.getStatus() == null) {
                safetyBlock.setStatus("ACTIVE");
            }
            if (safetyBlock.getIsActive() == null) {
                safetyBlock.setIsActive(true);
            }
            if (safetyBlock.getAppealSubmitted() == null) {
                safetyBlock.setAppealSubmitted(false);
            }
            if (safetyBlock.getTriggeredBySystem() == null) {
                safetyBlock.setTriggeredBySystem(false);
            }
            if (safetyBlock.getHumanReviewRequired() == null) {
                safetyBlock.setHumanReviewRequired(true);
            }
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(safetyBlock.getConnectId());
            docRef.set(convertToMap(safetyBlock)).get();
            
            return safetyBlock;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to create safety block", e);
        }
    }

    @Override
    public Optional<SafetyBlock> findByConnectId(String connectId) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME)
                    .document(connectId)
                    .get()
                    .get();
                    
            return doc.exists() ? Optional.of(convertToSafetyBlock(doc)) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find safety block by connectId", e);
        }
    }

    @Override
    public List<SafetyBlock> findByUserId(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .orderBy("blockedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToSafetyBlock)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find safety blocks by userId", e);
        }
    }

    @Override
    public List<SafetyBlock> findActiveSafetyBlocks(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isActive", true)
                    .orderBy("blockedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToSafetyBlock)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find active safety blocks", e);
        }
    }

    @Override
    public boolean isUserBlocked(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isActive", true)
                    .limit(1)
                    .get()
                    .get();
                    
            return !querySnapshot.getDocuments().isEmpty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to check if user is blocked", e);
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
            throw new RuntimeException("Failed to check if safety block exists", e);
        }
    }

    @Override
    public SafetyBlock updateSafetyBlock(SafetyBlock safetyBlock) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(safetyBlock.getConnectId());
            docRef.set(convertToMap(safetyBlock)).get();
            
            return safetyBlock;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update safety block", e);
        }
    }

    @Override
    public SafetyBlock updateStatus(String connectId, String status, Boolean isActive) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", status);
            if (isActive != null) updates.put("isActive", isActive);
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Safety block not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update safety block status", e);
        }
    }

    @Override
    public SafetyBlock liftBlock(String connectId, String liftedBy, Timestamp liftedAt) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "LIFTED");
            updates.put("isActive", false);
            updates.put("liftedBy", liftedBy);
            // Note: liftedAt would need to be added to the SafetyBlock model
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Safety block not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to lift safety block", e);
        }
    }

    @Override
    public SafetyBlock submitAppeal(String connectId, String appealReason, Timestamp appealedAt) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("appealSubmitted", true);
            updates.put("appealReason", appealReason);
            updates.put("appealedAt", appealedAt);
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Safety block not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to submit appeal", e);
        }
    }

    @Override
    public SafetyBlock reviewAppeal(String connectId, String appealReviewedBy, String appealDecision, Timestamp appealReviewedAt) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("appealReviewedBy", appealReviewedBy);
            updates.put("appealDecision", appealDecision);
            updates.put("appealReviewedAt", appealReviewedAt);
            
            // If appeal is overturned, lift the block
            if ("OVERTURNED".equals(appealDecision)) {
                updates.put("status", "LIFTED");
                updates.put("isActive", false);
            }
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Safety block not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to review appeal", e);
        }
    }

    @Override
    public SafetyBlock addReportId(String connectId, String reportId) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("reportIds", FieldValue.arrayUnion(reportId));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Safety block not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to add report ID", e);
        }
    }

    @Override
    public SafetyBlock updateRestrictedFeatures(String connectId, List<String> restrictedFeatures) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("restrictedFeatures", restrictedFeatures);
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Safety block not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update restricted features", e);
        }
    }

    @Override
    public void deleteSafetyBlock(String connectId) {
        try {
            firestore.collection(COLLECTION_NAME).document(connectId).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete safety block", e);
        }
    }

    @Override
    public List<SafetyBlock> findActiveBlocks() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("isActive", true)
                    .orderBy("blockedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToSafetyBlock)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find active blocks", e);
        }
    }

    @Override
    public List<SafetyBlock> findExpiredBlocks() {
        try {
            Timestamp now = Timestamp.now();
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("isActive", true)
                    .whereLessThan("expiresAt", now)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToSafetyBlock)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find expired blocks", e);
        }
    }

    @Override
    public List<SafetyBlock> findBlocksByType(String blockType) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("blockType", blockType)
                    .orderBy("blockedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToSafetyBlock)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find blocks by type", e);
        }
    }

    @Override
    public List<SafetyBlock> findBlocksBySeverity(String severity) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("severity", severity)
                    .orderBy("blockedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToSafetyBlock)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find blocks by severity", e);
        }
    }

    @Override
    public List<SafetyBlock> findBlocksNeedingReview() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("humanReviewRequired", true)
                    .whereEqualTo("reviewedBy", null)
                    .orderBy("blockedAt", Query.Direction.ASCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToSafetyBlock)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find blocks needing review", e);
        }
    }

    @Override
    public List<SafetyBlock> findPendingAppeals() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("appealSubmitted", true)
                    .whereEqualTo("appealReviewedBy", null)
                    .orderBy("appealedAt", Query.Direction.ASCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToSafetyBlock)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find pending appeals", e);
        }
    }

    @Override
    public List<SafetyBlock> findSystemTriggeredBlocks() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("triggeredBySystem", true)
                    .orderBy("blockedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToSafetyBlock)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find system triggered blocks", e);
        }
    }

    @Override
    public Optional<SafetyBlock> findById(String id) {
        return findByConnectId(id);
    }

    @Override
    public SafetyBlock save(SafetyBlock safetyBlock) {
        return updateSafetyBlock(safetyBlock);
    }

    // Helper conversion methods
    private Map<String, Object> convertToMap(SafetyBlock safetyBlock) {
        Map<String, Object> map = new HashMap<>();
        map.put("connectId", safetyBlock.getConnectId());
        map.put("userId", safetyBlock.getUserId());
        map.put("blockType", safetyBlock.getBlockType());
        map.put("severity", safetyBlock.getSeverity());
        map.put("reason", safetyBlock.getReason());
        map.put("blockedAt", safetyBlock.getBlockedAt());
        map.put("expiresAt", safetyBlock.getExpiresAt());
        map.put("duration", safetyBlock.getDuration());
        map.put("status", safetyBlock.getStatus());
        map.put("isActive", safetyBlock.getIsActive());
        map.put("createdBy", safetyBlock.getCreatedBy());
        map.put("reviewedBy", safetyBlock.getReviewedBy());
        map.put("liftedBy", safetyBlock.getLiftedBy());
        map.put("reportIds", safetyBlock.getReportIds());
        map.put("evidenceUrls", safetyBlock.getEvidenceUrls());
        map.put("context", safetyBlock.getContext());
        map.put("restrictedFeatures", safetyBlock.getRestrictedFeatures());
        map.put("warningMessage", safetyBlock.getWarningMessage());
        map.put("appealSubmitted", safetyBlock.getAppealSubmitted());
        map.put("appealedAt", safetyBlock.getAppealedAt());
        map.put("appealReason", safetyBlock.getAppealReason());
        map.put("appealReviewedBy", safetyBlock.getAppealReviewedBy());
        map.put("appealDecision", safetyBlock.getAppealDecision());
        map.put("appealReviewedAt", safetyBlock.getAppealReviewedAt());
        map.put("triggeredBySystem", safetyBlock.getTriggeredBySystem());
        map.put("triggerRule", safetyBlock.getTriggerRule());
        map.put("humanReviewRequired", safetyBlock.getHumanReviewRequired());
        map.put("adminNotes", safetyBlock.getAdminNotes());
        map.put("publicReason", safetyBlock.getPublicReason());
        return map;
    }

    private SafetyBlock convertToSafetyBlock(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) {
            throw new RuntimeException("Document data is null");
        }
        
        return SafetyBlock.builder()
                .connectId(doc.getId())
                .userId((String) data.get("userId"))
                .blockType((String) data.get("blockType"))
                .severity((String) data.get("severity"))
                .reason((String) data.get("reason"))
                .blockedAt((Timestamp) data.get("blockedAt"))
                .expiresAt((Timestamp) data.get("expiresAt"))
                .duration((Integer) data.get("duration"))
                .status((String) data.get("status"))
                .isActive((Boolean) data.get("isActive"))
                .createdBy((String) data.get("createdBy"))
                .reviewedBy((String) data.get("reviewedBy"))
                .liftedBy((String) data.get("liftedBy"))
                .reportIds((List<String>) data.get("reportIds"))
                .evidenceUrls((List<String>) data.get("evidenceUrls"))
                .context((String) data.get("context"))
                .restrictedFeatures((List<String>) data.get("restrictedFeatures"))
                .warningMessage((String) data.get("warningMessage"))
                .appealSubmitted((Boolean) data.get("appealSubmitted"))
                .appealedAt((Timestamp) data.get("appealedAt"))
                .appealReason((String) data.get("appealReason"))
                .appealReviewedBy((String) data.get("appealReviewedBy"))
                .appealDecision((String) data.get("appealDecision"))
                .appealReviewedAt((Timestamp) data.get("appealReviewedAt"))
                .triggeredBySystem((Boolean) data.get("triggeredBySystem"))
                .triggerRule((String) data.get("triggerRule"))
                .humanReviewRequired((Boolean) data.get("humanReviewRequired"))
                .adminNotes((String) data.get("adminNotes"))
                .publicReason((String) data.get("publicReason"))
                .build();
    }
}