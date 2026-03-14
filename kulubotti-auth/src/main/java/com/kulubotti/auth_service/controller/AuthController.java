package com.kulubotti.auth_service.controller;

import com.kulubotti.auth_service.dto.RegisterRequest;
import com.kulubotti.auth_service.entity.UserAccount;
import com.kulubotti.auth_service.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequest request) {
        // 1. Check if the user already exists
        if (userRepository.findByUsername(request.username()).isPresent()) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }

        // 2. Scramble the password with BCrypt
        String scrambledPassword = passwordEncoder.encode(request.password());

        // 3. Create the new user blueprint
        UserAccount newUser = new UserAccount(request.username(), scrambledPassword);

        // 4. Save it to the database
        userRepository.save(newUser);

        return ResponseEntity.ok("User registered successfully!");
    }
}