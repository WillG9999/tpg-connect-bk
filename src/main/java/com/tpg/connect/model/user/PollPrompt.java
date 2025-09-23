package com.tpg.connect.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PollPrompt {
    private String question;              // Poll question
    private String description;           // Poll description
    private List<String> options;         // Poll options
    private String selectedOption;        // User's selection
}