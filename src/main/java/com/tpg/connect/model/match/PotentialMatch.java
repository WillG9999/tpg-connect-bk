package com.tpg.connect.model.match;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PotentialMatch {
    private List<DiscoveryUser> users;
    private int count;
    private double latitude;
    private double longitude;
    private int maxDistance;
    private List<String> excludedUserIds;
}