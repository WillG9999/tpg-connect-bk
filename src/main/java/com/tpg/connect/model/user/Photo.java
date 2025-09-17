package com.tpg.connect.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Photo {
    private String id;
    private String url;
    private boolean isPrimary;
    private int order;
}