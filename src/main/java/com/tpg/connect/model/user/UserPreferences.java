package com.tpg.connect.model.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferences {
    private List<String> interestedIn;
    private AgeRange ageRange;
    private HeightRange heightRange;
    private Integer maxDistance;
    private List<String> dealBreakers;
    private List<String> mustHaves;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgeRange {
        private int min;
        private int max;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeightRange {
        private int min;
        private int max;
    }
    
    // Backward compatibility methods
    public Integer getMinAge() {
        return ageRange != null ? ageRange.getMin() : null;
    }
    
    public void setMinAge(Integer minAge) {
        if (ageRange == null) ageRange = new AgeRange();
        ageRange.setMin(minAge);
    }
    
    public Integer getMaxAge() {
        return ageRange != null ? ageRange.getMax() : null;
    }
    
    public void setMaxAge(Integer maxAge) {
        if (ageRange == null) ageRange = new AgeRange();
        ageRange.setMax(maxAge);
    }
    
    public Integer getMinHeight() {
        return heightRange != null ? heightRange.getMin() : null;
    }
    
    public void setMinHeight(Integer minHeight) {
        if (heightRange == null) heightRange = new HeightRange();
        heightRange.setMin(minHeight);
    }
    
    public Integer getMaxHeight() {
        return heightRange != null ? heightRange.getMax() : null;
    }
    
    public void setMaxHeight(Integer maxHeight) {
        if (heightRange == null) heightRange = new HeightRange();
        heightRange.setMax(maxHeight);
    }
    
    public String getPreferredGender() {
        return interestedIn != null && !interestedIn.isEmpty() ? interestedIn.get(0) : null;
    }
    
    public void setPreferredGender(String preferredGender) {
        if (interestedIn == null) interestedIn = new java.util.ArrayList<>();
        if (preferredGender != null) {
            interestedIn.clear();
            interestedIn.add(preferredGender);
        }
    }
    
    public String getDatingIntention() {
        return dealBreakers != null && !dealBreakers.isEmpty() ? dealBreakers.get(0) : null;
    }
    
    public void setDatingIntention(String datingIntention) {
        if (dealBreakers == null) dealBreakers = new java.util.ArrayList<>();
        if (datingIntention != null) {
            dealBreakers.clear();
            dealBreakers.add(datingIntention);
        }
    }
    
    public String getDrinkingPreference() {
        return mustHaves != null && !mustHaves.isEmpty() ? mustHaves.get(0) : null;
    }
    
    public void setDrinkingPreference(String drinkingPreference) {
        if (mustHaves == null) mustHaves = new java.util.ArrayList<>();
        if (drinkingPreference != null && !mustHaves.contains(drinkingPreference)) {
            mustHaves.add(drinkingPreference);
        }
    }
    
    public String getSmokingPreference() {
        return mustHaves != null && mustHaves.size() > 1 ? mustHaves.get(1) : null;
    }
    
    public void setSmokingPreference(String smokingPreference) {
        if (mustHaves == null) mustHaves = new java.util.ArrayList<>();
        if (smokingPreference != null) {
            if (mustHaves.size() < 2) mustHaves.add(smokingPreference);
            else mustHaves.set(1, smokingPreference);
        }
    }
}