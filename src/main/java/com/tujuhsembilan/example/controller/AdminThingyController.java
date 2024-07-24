package com.tujuhsembilan.example.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/admin")
public class AdminThingyController {

    @PostMapping("/do-something")
    public ResponseEntity<?> doSomething() {
        String message = "You have done something!";
        return ResponseEntity.ok(message);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }

        Jwt jwt = (Jwt) auth.getPrincipal();
        String username = jwt.getClaimAsString("sub");
        List<String> roles = jwt.getClaimAsStringList("roles");

        if (!roles.contains("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not ADMIN");
        } else {
            Map<String, Object> profile = new HashMap<>();
            profile.put("username", username);
            profile.put("roles", roles);
            return ResponseEntity.ok(profile);
        }
    }
}
