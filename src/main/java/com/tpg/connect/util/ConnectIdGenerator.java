package com.tpg.connect.util;

import com.tpg.connect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

@Component
public class ConnectIdGenerator {

    private static final long MIN_CONNECT_ID = 100000000000L; // 12 digits
    private static final long MAX_CONNECT_ID = 999999999999L;
    private final Random random = new SecureRandom();

    @Autowired
    private UserRepository userRepository;

    public String generateUniqueConnectId() {
        String connectId;
        int attempts = 0;
        do {
            if (attempts++ > 10) {
                throw new RuntimeException("Failed to generate unique ConnectID after 10 attempts");
            }
            connectId = generateConnectId();
        } while (userRepository.existsByConnectId(connectId));
        
        return connectId;
    }

    private String generateConnectId() {
        long randomId = MIN_CONNECT_ID + (long) (random.nextDouble() * (MAX_CONNECT_ID - MIN_CONNECT_ID));
        return String.valueOf(randomId);
    }
}