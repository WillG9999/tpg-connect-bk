package com.tpg.connect.model.user;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserPreferences {
    private String preferredGender;
    private int minAge;
    private int maxAge;
    private int minHeight;
    private int maxHeight;
    private String datingIntention;
    private String drinkingPreference;
    private String smokingPreference;
}