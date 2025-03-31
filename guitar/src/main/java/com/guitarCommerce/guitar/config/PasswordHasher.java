package com.guitarCommerce.guitar.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasher {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String plainPassword = "user123"; // Sostituisci con la tua password in chiaro
        String hashedPassword = encoder.encode(plainPassword);
        System.out.println("");

        System.out.println(hashedPassword);

        System.out.println("");

    }
}