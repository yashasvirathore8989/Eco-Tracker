package com.ecotracker.service;

import com.ecotracker.dto.RegistrationRequest;
import com.ecotracker.dto.UserResponse;
import com.ecotracker.entity.Role;
import com.ecotracker.entity.RoleType;
import com.ecotracker.entity.User;
import com.ecotracker.entity.UserStreak;
import com.ecotracker.exception.DuplicateResourceException;
import com.ecotracker.exception.ResourceNotFoundException;
import com.ecotracker.exception.ValidationException;
import com.ecotracker.repository.RoleRepository;
import com.ecotracker.repository.UserRepository;
import com.ecotracker.repository.UserStreakRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserStreakRepository userStreakRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       UserStreakRepository userStreakRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userStreakRepository = userStreakRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(RegistrationRequest request) {
        if (!request.isPasswordMatching()) {
            throw new ValidationException("Passwords do not match");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("An account with email " + normalizedEmail + " already exists.");
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setEnabled(true);

        // Security rule: public registration only ever grants ROLE_USER
        Role userRole = roleRepository.findByName(RoleType.ROLE_USER.name())
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_USER.name())));
        user.addRole(userRole);

        User savedUser = userRepository.save(user);

        // Initialize user streak record
        UserStreak streak = new UserStreak(savedUser);
        userStreakRepository.save(streak);
        savedUser.setStreak(streak);

        return savedUser;
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        UserStreak streak = user.getStreak() != null ? user.getStreak() : new UserStreak(user);

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                roles,
                user.getCreatedAt(),
                user.getStreak() != null ? 0L : 0L, // Will be enriched or calculated
                streak.getCurrentStreak(),
                streak.getLongestStreak()
        );
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}
