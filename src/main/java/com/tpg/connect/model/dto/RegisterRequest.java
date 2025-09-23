package com.tpg.connect.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.Period;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]*$", 
             message = "Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character")
    private String password;
    
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
    
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @NotBlank(message = "Date of birth is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date of birth must be in YYYY-MM-DD format")
    private String dateOfBirth;
    
    @NotBlank(message = "Gender is required")
    private String gender;
    
    @Size(max = 100, message = "Location must be less than 100 characters")
    private String location;
    
    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
    
    @AssertTrue(message = "User must be at least 18 years old")
    public boolean isAgeValid() {
        if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) return false;
        try {
            LocalDate birthDate = LocalDate.parse(dateOfBirth);
            return Period.between(birthDate, LocalDate.now()).getYears() >= 18;
        } catch (Exception e) {
            return false;
        }
    }
}