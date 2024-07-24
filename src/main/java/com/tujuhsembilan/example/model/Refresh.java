package com.tujuhsembilan.example.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table
@Entity(name = "refresh_token")
public class Refresh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
      private String username;
      private Instant expiryDate;
      private String deviceId;

}
