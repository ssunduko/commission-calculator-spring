package com.chapman.edu.commissions.architecture.verticalslice.features.authentication;

import com.chapman.edu.commissions.architecture.verticalslice.domain.User;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.data.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        request.validate();

        User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!user.isActive()) {
            throw new AuthenticationException("Account is deactivated");
        }

        if (user.getPasswordHash() == null
            || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid username or password");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.issueToken(user.getId(), user.getUsername());

        return new LoginResponse(
            token,
            "Bearer",
            jwtService.getExpirationSeconds(),
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName()
        );
    }
}
