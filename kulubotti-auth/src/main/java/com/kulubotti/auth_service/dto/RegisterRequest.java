package com.kulubotti.auth_service.dto;

// A Record is a modern, lightweight Java class perfect for holding incoming data
public record RegisterRequest(String username, String password) {
}