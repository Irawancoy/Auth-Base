package com.tujuhsembilan.example.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.ECKey;
import com.tujuhsembilan.example.configuration.property.AuthProp;
import com.tujuhsembilan.example.model.Refresh;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import com.tujuhsembilan.example.repository.RefreshTokenRepo;

@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BasicLoginController {
  

private final ObjectMapper objMap;
    private final JwtEncoder jwtEncoder;
    private final AuthProp authProp;
    private final ECKey ecJwk;
    private final RefreshTokenRepo refreshTokenRepo;

  @GetMapping("/jwks.json")
  public ResponseEntity<?> jwk() throws JsonProcessingException {
    return ResponseEntity.ok(Map.of("keys", Set.of(objMap.readTree(ecJwk.toPublicJWK().toJSONString()))));
  }

  // You MUST login using BASIC AUTH, NOT POST BODY
  @PostMapping("/login")
  public ResponseEntity<?> login(@NotNull Authentication auth) {
    User user = (User) auth.getPrincipal();

    long expirationTime = 5L;// exp dalam detik
    var jwt = jwtEncoder
        .encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.ES512).build(),
            JwtClaimsSet.builder()
                .issuer(authProp.getUuid())
                .audience(List.of(authProp.getUuid()))
                .subject(user.getUsername())
                .claim("roles", user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()))
                .expiresAt(Instant.now().plusSeconds(expirationTime))
                .build()));

    //buat refresh token ke repo dengan username 
    Refresh refresh = new Refresh();
    refresh.setUsername(user.getUsername());
    refresh.setToken(UUID.randomUUID().toString());
    refresh.setExpiryDate(Instant.now().plusSeconds(604800L));// 7 hari
    refreshTokenRepo.save(refresh);

    // Create a response object with user details, roles, and JWT token
    Map<String, Object> response = new HashMap<>();
    response.put("user", Map.of(
        "username", user.getUsername(),
        "roles", user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList())));
    response.put("token", jwt.getTokenValue());
    response.put("refresh_token", refresh.getToken());

    return ResponseEntity.ok(response);
  }

@PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String requestRefreshToken = request.get("refreshToken");
        Refresh refreshToken = refreshTokenRepo.findByToken(requestRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token tidak ditemukan!"));

        // Buat access token baru
        User user = new User(refreshToken.getUsername(), "", List.of()); // Sesuaikan dengan detail pengguna
        long expirationTime = 3600L; // 1 jam
        var jwt = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.ES512).build(),
                JwtClaimsSet.builder()
                        .issuer(authProp.getUuid())
                        .audience(List.of(authProp.getUuid()))
                        .subject(user.getUsername())
                        .claim("roles", List.of()) // Sesuaikan dengan role pengguna
                        .expiresAt(Instant.now().plusSeconds(expirationTime))
                        .build()));
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt.getTokenValue());

        return ResponseEntity.ok(response);
    }

}
