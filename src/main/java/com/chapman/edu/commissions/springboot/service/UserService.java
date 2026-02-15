package com.chapman.edu.commissions.springboot.service;

import com.chapman.edu.commissions.model.User;
import com.chapman.edu.commissions.model.UserRole;
import com.chapman.edu.commissions.springboot.dto.request.CreateUserRequest;
import com.chapman.edu.commissions.springboot.exception.BusinessValidationException;
import com.chapman.edu.commissions.springboot.exception.ResourceNotFoundException;
import com.chapman.edu.commissions.springboot.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for User management business logic.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(CreateUserRequest request) {
        // Business rule: username must be unique
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessValidationException(
                "Username '" + request.getUsername() + "' is already taken");
        }

        User user = new User(
            request.getUsername(),
            request.getEmail(),
            request.getFirstName(),
            request.getLastName()
        );

        // Hash the password before storing
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDepartment(request.getDepartment());
        user.setTerritory(request.getTerritory());
        user.setManagerId(request.getManagerId());

        // Assign roles
        if (request.getRoles() != null) {
            for (String role : request.getRoles()) {
                try {
                    user.addRole(UserRole.valueOf(role));
                } catch (IllegalArgumentException e) {
                    throw new BusinessValidationException("Invalid role: " + role);
                }
            }
        } else {
            user.addRole(UserRole.SALES_REP); // Default role
        }

        User saved = userRepository.save(user);
        logger.info("Created user: {} (ID: {})", saved.getUsername(), saved.getId());
        return saved;
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public User deactivateUser(String id) {
        User user = getUserById(id);
        user.setActive(false);
        logger.info("Deactivated user: {}", id);
        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepository.deleteById(id);
        logger.info("Deleted user: {}", id);
    }

    public long getUserCount() {
        return userRepository.count();
    }
}
