package com.tpg.connect.model.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    @Size(min = 2, max = 50)
    private String name;
    
    @Size(max = 500)
    private String bio;
    
    @Size(max = 100)
    private String location;
    
    @Size(max = 10)
    private List<String> interests;
    
    private List<String> languages;
    private String jobTitle;
    private String company;
}