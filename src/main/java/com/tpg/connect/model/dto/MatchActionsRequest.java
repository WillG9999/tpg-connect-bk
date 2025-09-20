package com.tpg.connect.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchActionsRequest {
    @NotBlank
    private String matchSetId;
    
    @NotEmpty
    private List<UserActionDto> actions;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserActionDto {
        @NotBlank
        private String targetUserId;
        
        @NotBlank
        private String action; // "LIKE" or "PASS"
    }
}