package com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out;

import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Output port for User persistence operations.
 */
public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(String id);

    List<User> findAll();
}
