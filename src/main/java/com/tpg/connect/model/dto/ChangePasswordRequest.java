package com.tpg.connect.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    @NotBlank(message = "Current password is required")
    private String currentPassword;
    
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]*$", 
             message = "New password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character")
    private String newPassword;
    
    @NotBlank(message = "Password confirmation is required")
    private String confirmNewPassword;
    
    @AssertTrue(message = "New passwords do not match")
    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmNewPassword);
    }
    
    @AssertTrue(message = "New password must be different from current password")
    public boolean isPasswordDifferent() {
        return currentPassword != null && newPassword != null && !currentPassword.equals(newPassword);
    }
}