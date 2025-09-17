package com.tpg.connect.model.user;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FieldVisibility {
    private boolean jobTitle = true;
    private boolean company = true;
    private boolean university = true;
    private boolean religiousBeliefs = true;
    private boolean politics = true;
    private boolean hometown = true;
    private boolean height = true;
    private boolean ethnicity = true;
}