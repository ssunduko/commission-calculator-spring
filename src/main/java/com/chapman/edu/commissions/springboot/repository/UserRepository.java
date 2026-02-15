package com.chapman.edu.commissions.springboot.repository;

import com.chapman.edu.commissions.model.User;
import com.chapman.edu.commissions.model.UserRole;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * HashMap-based repository for User entities.
 *
 * CONCEPT: @Autowired (Implicit)
 * --------------------------------
 * When Spring creates this bean, it can be injected into other beans using
 * @Autowired. With constructor injection (the preferred approach), the
 * @Autowired annotation is actually OPTIONAL if there's only one constructor:
 *
 *   @Service
 *   public class UserService {
 *       private final UserRepository userRepository;
 *
 *       // @Autowired is optional here — Spring infers it
 *       public UserService(UserRepository userRepository) {
 *           this.userRepository = userRepository;
 *       }
 *   }
 *
 * Spring 4.3+ automatically uses constructor injection when a class has
 * a single constructor, without requiring @Autowired.
 */
@Repository
public class UserRepository {

    private final Map<String, User> users = new ConcurrentHashMap<>();

    public User save(User user) {
        if (user.getId() == null || user.getId().isEmpty()) {
            user.setId(UUID.randomUUID().toString());
        }
        users.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    public Optional<User> findByUsername(String username) {
        return users.values().stream()
                .filter(user -> username.equals(user.getUsername()))
                .findFirst();
    }

    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(user -> email.equals(user.getEmail()))
                .findFirst();
    }

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public List<User> findByRole(UserRole role) {
        return users.values().stream()
                .filter(user -> user.hasRole(role))
                .collect(Collectors.toList());
    }

    public List<User> findByDepartment(String department) {
        return users.values().stream()
                .filter(user -> department.equals(user.getDepartment()))
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        users.remove(id);
    }

    public boolean existsById(String id) {
        return users.containsKey(id);
    }

    public boolean existsByUsername(String username) {
        return users.values().stream()
                .anyMatch(user -> username.equals(user.getUsername()));
    }

    public long count() {
        return users.size();
    }
}
