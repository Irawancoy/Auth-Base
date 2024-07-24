package com.tujuhsembilan.example.configuration;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tujuhsembilan.example.model.Refresh;
import com.tujuhsembilan.example.repository.RefreshTokenRepo;

import java.time.Instant;
import java.util.List;

@Component
public class TokenScheduler {

    private final RefreshTokenRepo refreshTokenRepo;

    public TokenScheduler(RefreshTokenRepo refreshTokenRepo) {
        this.refreshTokenRepo = refreshTokenRepo;
    }

    @Scheduled(fixedRate = 300000) // 5 menit (300000 ms)
    @Transactional
    public void refreshExpiredTokens() {
        List<Refresh> expiredTokens = refreshTokenRepo.findAllByExpiryDateBefore(Instant.now());

        for (Refresh token : expiredTokens) {
            token.setExpiryDate(Instant.now().plusSeconds(604800L)); // 7 hari
            refreshTokenRepo.save(token);
        }
    }
}
