package com.tujuhsembilan.example.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/admin")
public class AdminThingyController {

    @PreAuthorize("hasRole('ADMIN')") // Restrict access to users with ADMIN role
    @PostMapping("/do-something")
    public ResponseEntity<?> doSomething() {
      String message = "You have done something!";
      return ResponseEntity.ok(message);
    }


     @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return ResponseEntity.ok(userDetails);
        } else {
            return ResponseEntity.status(401).body("User is not authenticated");
        }
    }

    
    
}
