package com.finance.service.impl;

import com.finance.entity.RefreshToken;
import com.finance.entity.User;
import com.finance.exception.BadRequestException;
import com.finance.exception.ResourceNotFoundException;
import com.finance.repository.RefreshTokenRepository;
import com.finance.repository.UserRepository;
import com.finance.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final int LOCK_BUCKETS = 256;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final Object[] refreshTokenLocks = IntStream.range(0, LOCK_BUCKETS)
            .mapToObj(i -> new Object())
            .toArray(Object[]::new);

    @Override
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        synchronized (resolveLock(userId)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

            Optional<RefreshToken> existingToken = refreshTokenRepository.findByUser(user);
            if (existingToken.isPresent()) {
                RefreshToken refreshToken = existingToken.get();
                refreshToken.setToken(UUID.randomUUID().toString());
                refreshToken.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
                return refreshTokenRepository.save(refreshToken);
            }

            RefreshToken refreshToken = RefreshToken.builder()
                    .token(UUID.randomUUID().toString())
                    .user(user)
                    .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                    .build();

            return refreshTokenRepository.save(refreshToken);
        }
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new BadRequestException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    private Object resolveLock(Long userId) {
        return refreshTokenLocks[Math.floorMod(userId.hashCode(), LOCK_BUCKETS)];
    }

}