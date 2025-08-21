package com.spring.workspacemanager.controller;

import com.spring.workspacemanager.dto.RegisterRequest;
import com.spring.workspacemanager.model.User;
import com.spring.workspacemanager.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")

public class AuthController {

     private final AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (authService.emailExists(request.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email already in use");
        }

        String result = String.valueOf(authService.register(request));
        return ResponseEntity.ok(result);
    }
}
