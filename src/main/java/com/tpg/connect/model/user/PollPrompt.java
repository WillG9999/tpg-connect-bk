package com.tpg.connect.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PollPrompt {
    private String prompt;
    private String question;
    private List<String> options;
}