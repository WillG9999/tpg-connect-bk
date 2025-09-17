package com.tpg.connect.model.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CompleteUserProfile {
    private String id;
    private String name;
    private int age;
    private String bio;
    private List<Photo> photos;
    private String location;
    private List<String> interests;
    private UserProfile profile;
    private List<WrittenPrompt> writtenPrompts;
    private List<PollPrompt> pollPrompts;
    private FieldVisibility fieldVisibility;
    private UserPreferences preferences;
}