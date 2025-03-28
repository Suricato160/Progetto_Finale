package com.guitarCommerce.guitar.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.guitarCommerce.guitar.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {


    // Query per trovare un utente per username
    Optional<User> findByUsername(String username);

    // Query per trovare un utente per email
    Optional<User> findByEmail(String email);
}
