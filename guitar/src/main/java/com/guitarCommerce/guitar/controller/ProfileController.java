package com.guitarCommerce.guitar.controller;

import com.guitarCommerce.guitar.entity.User;
import com.guitarCommerce.guitar.service.UserService;

import java.security.Principal;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String showProfile(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword, 
                                 @RequestParam String newPassword, 
                                 @RequestParam String confirmPassword, 
                                 Principal principal, 
                                 RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(principal.getName());
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Password attuale errata");
        } else if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Le nuove password non coincidono");
        } else {
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "Password aggiornata con successo");
        }
        return "redirect:/profile";
    }

    @PostMapping("/update-address")
    public String updateAddress(@ModelAttribute User user, Principal principal) {
        User currentUser = userService.findByUsername(principal.getName());
        currentUser.setStreet(user.getStreet());
        currentUser.setCity(user.getCity());
        currentUser.setPostalCode(user.getPostalCode());
        currentUser.setCountry(user.getCountry());
        userService.updateUser(currentUser);
        return "redirect:/profile";
    }


    @PostMapping("/update-phone")
    public String updatePhone(@ModelAttribute("user") User user) {
        User currentUser = userService.getCurrentUser();
        currentUser.setPhone(user.getPhone()); // Aggiorna solo il campo telefono
        userService.updateUser(currentUser);
        return "redirect:/profile";
    }
}