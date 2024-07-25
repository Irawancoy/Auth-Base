package com.tujuhsembilan.example.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tujuhsembilan.example.model.Refresh;
import com.tujuhsembilan.example.repository.RefreshTokenRepo;

import java.time.Instant;
import java.util.List;

@Component
public class TokenScheduler {

   private static final Logger logger = LoggerFactory.getLogger(TokenScheduler.class);

   @Autowired
   private RefreshTokenRepo refreshTokenRepo;

   //1 menit
   @Scheduled(fixedRate = 60000)
    @Transactional
    public void refreshExpiredTokens() {
        try {
            List<Refresh> expiredTokens = refreshTokenRepo.findAllByExpiryDateBefore(Instant.now());
            if (expiredTokens.isEmpty()) {
                logger.info("No expired tokens found to refresh.");
                return;
            }

            for (Refresh token : expiredTokens) {
                token.setExpiryDate(Instant.now().plusSeconds(604800L)); // 7 hari
                refreshTokenRepo.save(token);
            }

            logger.info("Refreshed {} expired tokens.", expiredTokens.size());
        } catch (Exception e) {
            logger.error("Error occurred while refreshing tokens: ", e);
        }
    }
}
