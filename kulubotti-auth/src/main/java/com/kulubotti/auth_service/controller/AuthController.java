package com.kulubotti.auth_service.controller;

import com.kulubotti.auth_service.dto.LoginRequest;
import com.kulubotti.auth_service.dto.RegisterRequest;
import com.kulubotti.auth_service.entity.UserAccount;
import com.kulubotti.auth_service.repository.UserRepository;
import com.kulubotti.auth_service.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Spring injects all three tools here
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            return ResponseEntity.badRequest().body("Username is already taken!");
        }
        String scrambledPassword = passwordEncoder.encode(request.password());
        UserAccount newUser = new UserAccount(request.username(), scrambledPassword);
        userRepository.save(newUser);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody LoginRequest request) {
        // 1. Find the user in the database
        Optional<UserAccount> userOptional = userRepository.findByUsername(request.username());

        if (userOptional.isPresent()) {
            UserAccount user = userOptional.get();
            // 2. Check if the typed password matches the scrambled BCrypt password
            if (passwordEncoder.matches(request.password(), user.getPassword())) {
                // 3. Passwords match! Generate the VIP Wristband (JWT)
                String token = jwtService.generateToken(user.getUsername());
                return ResponseEntity.ok(token);
            }
        }
        // If we get here, either the user wasn't found, or the password was wrong
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
    }
}