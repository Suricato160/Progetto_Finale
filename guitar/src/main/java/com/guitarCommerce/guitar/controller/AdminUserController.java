package com.guitarCommerce.guitar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.guitarCommerce.guitar.entity.User;
import com.guitarCommerce.guitar.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    // Mostra la lista di tutti gli utenti
    @GetMapping
    public String getAllUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "users/usersList";
    }

    // Mostra i dettagli di un utente
    @GetMapping("/{id}")
    public String getUserById(@PathVariable Integer id, Model model) {
        User user = userService.getUserById(id);
        if (user == null) {
            model.addAttribute("error", "Utente non trovato.");
        }
        model.addAttribute("user", user);
        return "users/userDetail";
    }

    // Elimina un utente
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    // Aggiorna il ruolo di un utente
    @PostMapping("/{id}/update-role")
    public String updateUserRole(@PathVariable Integer id, @RequestParam("role") String role, Model model) {
        User user = userService.getUserById(id);
        if (user == null) {
            model.addAttribute("error", "Utente non trovato.");
            return "users/userDetail";
        }
        user.setRole(role);
        userService.updateUser(id, user);
        model.addAttribute("success", "Ruolo aggiornato con successo.");
        model.addAttribute("user", user);
        return "users/userDetail";
    }
}