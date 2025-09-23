package com.tpg.connect.model.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettings {
    private boolean newMatches = true;
    private boolean messages = true;
    private boolean likes = true;
    private boolean superLikes = true;
    private boolean promotions = false;
    private boolean emailUpdates = false;
}