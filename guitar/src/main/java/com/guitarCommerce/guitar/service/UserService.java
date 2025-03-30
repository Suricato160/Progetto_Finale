package com.guitarCommerce.guitar.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guitarCommerce.guitar.entity.User;
import com.guitarCommerce.guitar.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Trova tutti gli utenti
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Trova un utente per ID
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User non trovato con id: " + id));
    }

    // Trova un utente per username
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User non trovato con username: " + username));
    }

    // Ottieni l'utente corrente autenticato
    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return findByUsername(username);
    }

    // Creazione di un nuovo utente
    @Transactional
    public User createUser(User user) {
        // Se username già esistente solleva un'eccezione
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username già in uso");
        }
        return userRepository.save(user);
    }

    // Aggiorna un utente esistente (versione originale con ID)
    @Transactional
    public User updateUser(Integer id, User updatedUser) {
        User user = getUserById(id);
        user.setUsername(updatedUser.getUsername());
        user.setPassword(updatedUser.getPassword());
        user.setEmail(updatedUser.getEmail());
        user.setRole(updatedUser.getRole());
        return userRepository.save(user);
    }

    // Aggiorna un utente esistente (versione sovraccaricata senza ID)
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

   

    // Elimina un utente
    @Transactional
    public void deleteUser(Integer id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
}