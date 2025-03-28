package com.guitarCommerce.guitar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.guitarCommerce.guitar.entity.User;
import com.guitarCommerce.guitar.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Mostra la lista di tutti gli utenti
    @GetMapping
    public String getAllUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "list"; // Nome del template Thymeleaf (es. users/list.html)
    }

    // Mostra i dettagli di un utente
    @GetMapping("/{id}")
    public String getUserById(@PathVariable Integer id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "users/detail"; // Template per i dettagli dell'utente
    }

    // Mostra il form per creare un nuovo utente
    @GetMapping("/new")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new User());
        return "users/form"; // Template per il form di creazione
    }

    // Salva un nuovo utente
    @PostMapping
    public String createUser(@ModelAttribute User user) {
        userService.createUser(user);
        return "redirect:/users"; // Redirect alla lista degli utenti
    }

    // Mostra il form per aggiornare un utente
    @GetMapping("/{id}/edit")
    public String showUpdateUserForm(@PathVariable Integer id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "users/form"; // Stesso template del form di creazione
    }

    // Aggiorna un utente esistente
    @PostMapping("/{id}")
    public String updateUser(@PathVariable Integer id, @ModelAttribute User user) {
        userService.updateUser(id, user);
        return "redirect:/users"; // Redirect alla lista
    }

    // Elimina un utente
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return "redirect:/users"; // Redirect alla lista
    }
}