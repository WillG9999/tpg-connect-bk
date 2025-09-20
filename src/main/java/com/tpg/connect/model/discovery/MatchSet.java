package com.tpg.connect.model.discovery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "match_sets")
public class MatchSet {
    @Id
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