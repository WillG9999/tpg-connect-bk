package com.tpg.connect.model.dto;

import com.tpg.connect.model.match.UserAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionRequest {
    @NotBlank
    private String targetUserId;
    
    @NotNull
    private UserAction.ActionType action;
    
    private String batchDate;
    private LocalDateTime timestamp;
}