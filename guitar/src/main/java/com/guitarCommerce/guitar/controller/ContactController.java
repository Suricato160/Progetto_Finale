package com.guitarCommerce.guitar.controller;

import java.security.Principal;

import org.springframework.boot.autoconfigure.amqp.RabbitConnectionDetails.Address;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContactController {


    @GetMapping("/contact")
    public String showContactForm() {
        return "contact"; // Nome del template: contact.html
    }

    @PostMapping("/contact")
    public String submitContactForm(@RequestParam String name, 
                                   @RequestParam String email, 
                                   @RequestParam String message, 
                                   Model model) {
        // Qui puoi elaborare il messaggio (es. inviarlo via email o salvarlo)
        model.addAttribute("successMessage", "Grazie, " + name + "! Il tuo messaggio è stato inviato.");
        return "contact";
    }
}