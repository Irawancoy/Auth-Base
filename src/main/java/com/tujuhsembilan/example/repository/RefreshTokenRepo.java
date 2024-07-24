package com.tujuhsembilan.example.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tujuhsembilan.example.model.Refresh;

public interface RefreshTokenRepo extends JpaRepository<Refresh, Long> {
   Optional<Refresh> findByToken(String token);

   void deleteAllByUsername(String username);

   List<Refresh> findAllByExpiryDateBefore(Instant now);
   
   Optional<Refresh> findByUsernameAndDeviceId(String username, String deviceId);
}