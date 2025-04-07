package com.guitarCommerce.guitar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.guitarCommerce.guitar.service.UserService;


// ======================================= ok


@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    // dipendenze
    @Autowired
    private UserService userService;

    // =======================================
    // Mostra la lista di tutti gli utenti
    @GetMapping
    public String getAllUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "users/usersList";
    }

    // =======================================
    // Mostra i dettagli di un utente
    @GetMapping("/{id}")
    public String getUserById(@PathVariable Integer id, Model model) {
        userService.getUserDetails(id, model);
        return "users/userDetail";
    }

    // =======================================
    // Elimina un utente
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("success", "Utente eliminato con successo.");
        return "redirect:/admin/users";
    }

    // =======================================
    // Aggiorna il ruolo di un utente
    @PostMapping("/{id}/update-role")
    public String updateUserRole(@PathVariable Integer id, @RequestParam("role") String role, Model model) {
        userService.updateUserRole(id, role, model);
        return "users/userDetail";
    }
}