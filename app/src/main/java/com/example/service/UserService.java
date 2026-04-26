package com.example.service;

import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // -----------------------------------
    // AUTH
    // ------------------------------------
    //REGISTER USER
    public boolean register(String email, String password, String username, Long favTeamId) {
        if (userRepository.findByEmail(email).isPresent()) return false;

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(password));
        user.setFavTeamId(favTeamId);

        userRepository.save(user);
        return true;
    }
    //LOGIN
    public User login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(u -> encoder.matches(password, u.getPasswordHash()))
                .orElse(null);
    }

    // ─────────────────────────────────────────
    // SETTINGS
    // ─────────────────────────────────────────

    // Update username — returns false if username already taken
    public boolean updateUsername(Long userId, String newUsername) {
        if (newUsername == null || newUsername.trim().isEmpty()) return false;

        Optional<User> existing = userRepository.findByUsername(newUsername.trim());
        if (existing.isPresent() && !existing.get().getUserId().equals(userId)) return false;

        userRepository.findById(userId).ifPresent(user -> {
            user.setUsername(newUsername.trim());
            userRepository.save(user);
        });
        return true;
    }

    // Change password — requires current password to verify
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) return false;

        return userRepository.findById(userId).map(user -> {
            if (!encoder.matches(currentPassword, user.getPasswordHash())) return false;
            user.setPasswordHash(encoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    // Update favourite team
    public void updateFavouriteTeam(Long userId, Long teamId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setFavTeamId(teamId);
            userRepository.save(user);
        });
    }

    // Delete account
    public void deleteAccount(Long userId) {
        userRepository.deleteById(userId);
    }

    // Get user by ID
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }
}