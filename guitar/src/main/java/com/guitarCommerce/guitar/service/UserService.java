package com.guitarCommerce.guitar.service;

import java.util.List;
import java.util.Optional;

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

    // Verifica se il numero di telefono è già in uso da un altro utente
    public boolean isPhoneNumberInUse(String phone, Integer currentUserId) {
        Optional<User> existingUser = userRepository.findByPhone(phone);
        return existingUser.isPresent() && existingUser.get().getId() != currentUserId;
    }

    // Metodo per validare il formato del numero di telefono
    public boolean isValidPhoneNumber(String phoneNumber) {
        // Espressione regolare per numeri di telefono (personalizzabile)
        String phoneRegex = "^\\+?[0-9]{1,3}?\\s?\\(?([0-9]{1,3}?)\\)?[\\s.-]?[0-9]{1,4}[\\s.-]?[0-9]{1,4}[\\s.-]?[0-9]{1,9}$";
        return phoneNumber != null && phoneNumber.matches(phoneRegex);
    }
}