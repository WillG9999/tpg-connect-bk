package com.tpg.connect.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WrittenPrompt {
    private String prompt;
    private String answer;
}