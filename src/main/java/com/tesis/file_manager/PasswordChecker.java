package com.tesis.file_manager;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordChecker {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "0508"; // la que intentas usar en Postman
        String hashedPassword = "$2a$10$8IcTQ5SnvEeTbwLXUzhvI.B0DW4yX9ECi9QK6cMOCx8ZrLqxnm/.K";

        System.out.println("¿Coincide?: " + encoder.matches(rawPassword, hashedPassword));
        System.out.println("Nueva contraseña: " + encoder.encode("0508"));

    }


}

