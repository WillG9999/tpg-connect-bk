package com.tpg.connect.model.discovery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchSet {
    private String id;
    private String userId;
    private LocalDate date;
    private List<String> userIds;
    private MatchSetStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private int actionsSubmitted;
    private int matchesFound;
}