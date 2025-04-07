package com.guitarCommerce.guitar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// ======================================= ok

@Controller
public class LoginController {
    @GetMapping("/login")
    public String login() {
        return "login"; 
    }
}