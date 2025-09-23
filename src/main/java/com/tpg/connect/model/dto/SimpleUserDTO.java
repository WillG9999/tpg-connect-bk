package com.tpg.connect.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleUserDTO {
    private String id;
    private String userId;
    private String firstName;
    private String lastName;
    private String name;
    private int age;
    private String gender;
    private String location;
    private boolean active;
    
    // Factory method to create from CompleteUserProfile
    public static SimpleUserDTO fromCompleteUserProfile(com.tpg.connect.model.user.CompleteUserProfile profile) {
        SimpleUserDTO dto = new SimpleUserDTO();
        dto.setId(profile.getId());
        dto.setUserId(profile.getUserId());
        dto.setFirstName(profile.getFirstName());
        dto.setLastName(profile.getLastName());
        dto.setName(profile.getName());
        dto.setAge(profile.getAge());
        dto.setGender(profile.getGender());
        dto.setLocation(profile.getLocation());
        dto.setActive(profile.getActive());
        return dto;
    }
}