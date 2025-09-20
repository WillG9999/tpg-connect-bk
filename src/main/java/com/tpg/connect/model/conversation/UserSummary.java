package com.tpg.connect.model.conversation;

import com.tpg.connect.model.user.Photo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSummary {
    private String id;
    private String name;
    private int age;
    private List<Photo> photos;
}