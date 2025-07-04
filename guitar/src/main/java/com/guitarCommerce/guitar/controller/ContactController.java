package com.guitarCommerce.guitar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// ======================================= ok

@Controller
public class ContactController {


    @GetMapping("/contact")
    public String showContactForm() {
        return "contact"; 
    }

    @PostMapping("/contact")
    public String submitContactForm(@RequestParam String name, 
                                   @RequestParam String email, 
                                   @RequestParam String message, 
                                   Model model) {
        model.addAttribute("successMessage", "Grazie, " + name + "! Il tuo messaggio è stato inviato.");
        return "contact";
    }
}