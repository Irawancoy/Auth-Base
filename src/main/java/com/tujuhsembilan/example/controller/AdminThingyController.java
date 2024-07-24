package com.tujuhsembilan.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/admin")
public class AdminThingyController {

    @PostMapping("/do-something")
    public ResponseEntity<?> doSomething() {
        String message = "You have done something!";
        return ResponseEntity.ok(message);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // Ambil token JWT dari Authentication
            Jwt jwt = (Jwt) authentication.getPrincipal();
            
            // Ambil klaim role dari JWT
            List<String> roles = jwt.getClaimAsStringList("role");
            
            // Ambil username
            String username = jwt.getSubject();
            
            // Return response dengan username dan roles
            return ResponseEntity.ok(Map.of("username", username, "roles", roles));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }
    }
           
    
}
