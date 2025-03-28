package com.guitarCommerce.guitar.controller;

import com.guitarCommerce.guitar.entity.User;
import com.guitarCommerce.guitar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String showProfile(Model model) {
        // Esempio: utente fittizio, in un'app reale recupereresti l'utente autenticato
        User user = userService.getUserById(1); // Sostituisci con logica di autenticazione
        model.addAttribute("user", user);
        return "profile"; // Nome del template: profile.html
    }
}