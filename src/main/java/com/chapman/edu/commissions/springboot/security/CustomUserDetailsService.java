package com.chapman.edu.commissions.springboot.security;

import com.chapman.edu.commissions.model.User;
import com.chapman.edu.commissions.model.UserRole;
import com.chapman.edu.commissions.springboot.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * CUSTOM USER DETAILS SERVICE — SPRING SECURITY INTEGRATION
 * ============================================================================
 *
 * CONCEPT: UserDetailsService
 * ------------------------------
 * UserDetailsService is a core interface in Spring Security. It defines a
 * single method: loadUserByUsername(). Spring Security calls this during
 * authentication to load user data from your data store.
 *
 * The returned UserDetails object contains:
 *   - Username and password (for credential verification)
 *   - Granted authorities (roles/permissions for authorization)
 *   - Account status flags (enabled, locked, expired, etc.)
 *
 * CONCEPT: GrantedAuthority
 * ---------------------------
 * GrantedAuthority represents a permission or role assigned to a user.
 * Spring Security uses the "ROLE_" prefix convention:
 *   - UserRole.SALES_REP → ROLE_SALES_REP
 *   - UserRole.SYSTEM_ADMIN → ROLE_SYSTEM_ADMIN
 *
 * In authorization rules, hasRole("SALES_REP") automatically checks for
 * the "ROLE_SALES_REP" authority.
 *
 * CONCEPT: @Service
 * -------------------
 * @Service is a specialization of @Component that indicates this class
 * contains business logic. It's functionally identical to @Component but
 * communicates intent — this bean is part of the service/business layer.
 *
 * @see org.springframework.security.core.userdetails.UserDetailsService
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load a user by username for Spring Security authentication.
     *
     * This is called by Spring Security when:
     *   - A user submits a login form
     *   - A JWT token is validated (to load authorities)
     *   - Any authentication check requires user details
     *
     * @param username the username to search for
     * @return UserDetails containing the user's credentials and authorities
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find the user in our HashMap-based repository
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "User not found with username: " + username
                ));

        // Convert our UserRole enum values to Spring Security GrantedAuthority objects
        // Each role gets the "ROLE_" prefix required by Spring Security
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());

        // Return Spring Security's UserDetails implementation
        // This bridges our User model with Spring Security's authentication system
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                user.isActive(),           // enabled
                true,                       // accountNonExpired
                true,                       // credentialsNonExpired
                true,                       // accountNonLocked
                authorities                 // roles/permissions
        );
    }
}
