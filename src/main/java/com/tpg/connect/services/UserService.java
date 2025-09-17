package com.tpg.connect.services;

import com.tpg.connect.client.database.UserRepository;
import com.tpg.connect.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findById(String id) {
        return userRepository.findById(id);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return rawPassword.equals(encodedPassword);
    }

    public User createUser(String username, String email, String password, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        String userId = String.valueOf(System.currentTimeMillis());
        User user = new User(userId, username, email, password, role, true);
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        User existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser != null) {
            return userRepository.save(user);
        }
        throw new RuntimeException("User not found");
    }

    public boolean deactivateUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setActive(false);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public boolean activateUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setActive(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}