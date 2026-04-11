package com.example.service;

import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
//hashing passwords
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public boolean register(String email, String passwordHash, String username) {
        if (userRepository.findByEmail(email).isPresent()) {
            return false; // email already exists
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(passwordHash));
        user.setUsername(username);

        userRepository.save(user);
        return true;
    }

    public User login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(u -> encoder.matches(password, u.getPasswordHash()))
                .orElse(null);
    }
}

