package com.tujuhsembilan.example.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            User user = (User) auth.getPrincipal();
            List<String> roles =  ((User) auth.getPrincipal()).getAuthorities().stream()
                .map(a -> a.getAuthority()).collect(Collectors.toList());

            String jwtToken = jwtEncoder.encode(JwtEncoderParameters.from(
                    JwsHeader.with(SignatureAlgorithm.ES512).build(),
                    JwtClaimsSet.builder()
                            .issuer(authProp.getUuid())
                            .audience(List.of(authProp.getUuid()))
                            .subject(user.getUsername())
                            .claim("role", roles)
                            // Set expiration, claims, etc. here too
                            .build()
            )).getTokenValue();

            return ResponseEntity.ok(Map.of("token", jwtToken, "roles", roles));
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication failed"));
        }
    }
}
