package com.tpg.connect.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryRequest {
    @Min(1)
    @Max(50)
    private int count = 10;
    
    private Double latitude;
    private Double longitude;
    
    @Min(1)
    @Max(100)
    private Integer maxDistance;
    
    private List<String> excludeUserIds;
}