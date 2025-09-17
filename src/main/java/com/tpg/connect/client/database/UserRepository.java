package com.tpg.connect.client.database;

import com.tpg.connect.model.user.User;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserRepository {

    private final Map<String, User> users = new HashMap<>();

    public UserRepository() {
        users.put("admin", new User("1", "admin", "admin@example.com", "password", "ADMIN", true));
        users.put("user", new User("2", "user", "user@example.com", "password", "USER", true));
        users.put("alex", new User("user_123", "alex", "alex@example.com", "password", "USER", true));
    }

    public User findByUsername(String username) {
        return users.get(username);
    }

    public User findById(String id) {
        return users.values().stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public User save(User user) {
        users.put(user.getUsername(), user);
        return user;
    }

    public boolean existsByUsername(String username) {
        return users.containsKey(username);
    }

    public void deleteByUsername(String username) {
        users.remove(username);
    }
}