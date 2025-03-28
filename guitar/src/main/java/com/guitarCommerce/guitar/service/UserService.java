package com.guitarCommerce.guitar.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.guitarCommerce.guitar.entity.User;
import com.guitarCommerce.guitar.repository.UserRepository;

public class UserService {

    @Autowired
    private UserRepository userRepository;

    //Trova tutti gli utenti
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    //Trova un utente per ID
    public User getUserById(Integer id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User non trovato con id: "+ id));
    }

     //Trova un utente per usarname
     public User getUserByUser(Integer username) {
        return userRepository.findById(username).orElseThrow(() -> new RuntimeException("User non trovato con username: "+ username));
    }




    // Creazione di un nuovo utente
    @Transactional
    public User createUser(User user) {
        //se username già esistente solleva un eccezione
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username già in uso");
        }

        return userRepository.save(user);
    }


    // Aggiorna utente 
       // Aggiorna un utente esistente
       @Transactional
       public User updateUser(Integer id, User updatedUser) {
           User user = getUserById(id);
           user.setUsername(updatedUser.getUsername());
           user.setPassword(updatedUser.getPassword());
           user.setEmail(updatedUser.getEmail());
           user.setRole(updatedUser.getRole());
           return userRepository.save(user);
       }
   
       // Elimina un utente
       @Transactional
       public void deleteUser(Integer id) {
           User user = getUserById(id);
           userRepository.delete(user);
       }
    
}
