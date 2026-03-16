package com.chapman.edu.commissions.architecture.ddd.domain.user;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(String id);
}
