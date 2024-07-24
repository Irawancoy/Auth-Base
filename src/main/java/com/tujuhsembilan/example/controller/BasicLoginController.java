package com.tujuhsembilan.example.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.ECKey;
import com.tujuhsembilan.example.configuration.property.AuthProp;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BasicLoginController {

  private final ObjectMapper objMap;

  private final JwtEncoder jwtEncoder;
  private final AuthProp authProp;

  private final ECKey ecJwk;

  @GetMapping("/jwks.json")
  public ResponseEntity<?> jwk() throws JsonProcessingException {
    return ResponseEntity.ok(Map.of("keys", Set.of(objMap.readTree(ecJwk.toPublicJWK().toJSONString()))));
  }

  // You MUST login using BASIC AUTH, NOT POST BODY
  @PostMapping("/login")
  public ResponseEntity<?> login(@NotNull Authentication auth) {
    User user = (User) auth.getPrincipal();
    // expire 1 menit
    long expirationTime = 5L;
      var jwt = jwtEncoder
          .encode(JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.ES512).build(),
              JwtClaimsSet.builder()
                  .issuer(authProp.getUuid())
                  .audience(List.of(authProp.getUuid()))
                  .subject(user.getUsername())
                  .claim("roles", user.getAuthorities().stream()
                          .map(GrantedAuthority::getAuthority)
                      .collect(Collectors.toList()))
                 .expiresAt(Instant.now().plusSeconds(expirationTime)
                 )
                  .build()));
  
      // Create a response object with user details, roles, and JWT token
      Map<String, Object> response = new HashMap<>();
      response.put("user", Map.of(
          "username", user.getUsername(),
          "roles", user.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .collect(Collectors.toList())
      ));
      response.put("token", jwt.getTokenValue());
  
      return ResponseEntity.ok(response);
  }
  

}
