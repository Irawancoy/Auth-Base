package com.tujuhsembilan.example.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tujuhsembilan.example.model.Refresh;

public interface RefreshTokenRepo extends JpaRepository<Refresh, Long> {
   Optional<Refresh> findByToken(String token);
   void deleteByUsername(String username);
}