package com.tpg.connect.model.dto;

import com.tpg.connect.model.UserReport;
import com.tpg.connect.model.safety.ReportReason;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    @NotNull
    private ReportReason reason;
    
    @Size(max = 500)
    private String details;
    
    private LocalDateTime reportedAt;
}