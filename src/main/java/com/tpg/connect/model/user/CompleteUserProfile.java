package com.tpg.connect.model.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "users")
public class CompleteUserProfile {
    @Id
    private String id;
    
    @NotBlank
    @Size(min = 2, max = 50)
    private String name;
    
    @Min(18)
    @Max(100)
    private int age;
    
    @Size(max = 500)
    private String bio;
    
    @NotEmpty
    @Size(min = 1, max = 6)
    private List<Photo> photos;
    
    @Size(max = 100)
    private String location;
    
    @Size(max = 10)
    private List<String> interests;
    
    private UserProfile profile;
    private List<WrittenPrompt> writtenPrompts;
    private List<PollPrompt> pollPrompts;
    private FieldVisibility fieldVisibility;
    private UserPreferences preferences;
    
    // Missing fields needed by AuthenticationService
    private String userId;
    private String firstName;
    private String lastName;
    private java.time.LocalDate dateOfBirth;
    private String gender;
    private boolean active = true;
    private String jobTitle;
    private String university;
    
    // NoSQL metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastActive;
    private int version;
}