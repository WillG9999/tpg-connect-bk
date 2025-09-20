package com.tpg.connect.model.match;

import com.tpg.connect.model.user.Photo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryUser {
    private String id;
    private String name;
    private int age;
    private String bio;
    private List<Photo> photos;
    private String location;
    private List<String> interests;
    private Double distanceKm;
    
    // Basic profile info for discovery - no sensitive details
    private String pronouns;
    private String gender;
    private String jobTitle;
    private String university;
    private String height;
}