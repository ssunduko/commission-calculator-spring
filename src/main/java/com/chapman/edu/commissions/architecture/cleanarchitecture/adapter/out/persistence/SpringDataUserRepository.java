package com.chapman.edu.commissions.architecture.cleanarchitecture.adapter.out.persistence;

import com.chapman.edu.commissions.architecture.cleanarchitecture.application.port.out.UserRepositoryPort;
import com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataUserRepository extends JpaRepository<User, String>, UserRepositoryPort {
}
