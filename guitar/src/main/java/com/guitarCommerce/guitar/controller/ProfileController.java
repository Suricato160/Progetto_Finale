package com.guitarCommerce.guitar.controller;

import com.guitarCommerce.guitar.entity.User;
import com.guitarCommerce.guitar.service.UserService;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.beans.factory.annotation.Autowired;


// ============================ ok

@Controller
@RequestMapping("/profile")
public class ProfileController {

    // dipendenze
    @Autowired
    private UserService userService;

    // -----------------------------------------------

    // Mostra il profilo utente
    @GetMapping
    public String showProfile(Model model, Principal principal) { // principal per l'utente autenticato
        userService.showProfile(model);
        return "profile";
    }

    // -----------------------------------------------
    // Aggiorna l'username
    @PostMapping("/change-username")
    public String updateUsername(@RequestParam String username, Model model, Principal principal) {
        userService.updateUsername(username, principal, model);
        return "profile";
    }

    // -----------------------------------------------
    // Aggiorna la password
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {  
        userService.changePassword(currentPassword, newPassword, confirmPassword, principal, redirectAttributes);
        return "redirect:/profile";
    }

    // -----------------------------------------------
    // Aggiorna l'email
    @PostMapping("/change-Email")
    public String changeEmail(@RequestParam String currentMail,
                              @RequestParam String newMail,
                              Principal principal,
                              Model model) {
        userService.changeEmail(currentMail, newMail, principal, model);
        return "profile";
    }

    // -----------------------------------------------
    // Aggiorna il numero di telefono
    @PostMapping("/update-phone")
    public String updatePhone(@ModelAttribute("user") User user, Model model) {
        userService.updatePhone(user, model);
        return "profile";
    }

    // -----------------------------------------------
    // Aggiorna l'indirizzo
    @PostMapping("/update-address")
    public String updateAddress(@ModelAttribute User user, Principal principal) {
        userService.updateAddress(user, principal);
        return "redirect:/profile";
    }
}