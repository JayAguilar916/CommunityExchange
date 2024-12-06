package com.spring.jpa.repository;

import com.spring.jpa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {  // ID type should be Long
    User findByUsername(String username);  // Find user by username
}
