package com.tpg.connect.model.batch;

import com.tpg.connect.model.match.MutualMatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchResult {
    private String date;
    private boolean batchCompleted;
    private int totalReviewed;
    private List<MutualMatch> mutualMatches;
    private int totalMutualMatches;
}