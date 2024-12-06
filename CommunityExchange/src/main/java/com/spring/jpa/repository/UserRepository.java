package com.spring.jpa.repository;

import com.spring.jpa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

	User findByUsername(String username);
    // This will automatically provide a method to fetch a user by username
}
