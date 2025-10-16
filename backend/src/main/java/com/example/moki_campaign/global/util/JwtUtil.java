package com.example.moki_campaign.global.util;

import com.example.moki_campaign.global.config.JwtProps;
import com.example.moki_campaign.global.exception.common.BusinessException;
import com.example.moki_campaign.global.exception.common.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProps props;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private long requirePositiveSeconds(long seconds, String name) {
        if (seconds <= 0) {
            throw new IllegalStateException(name + " must be > 0 seconds");
        }
        return seconds;
    }

    public long getAccessTtl() {
        return requirePositiveSeconds(props.getAccessExpSeconds(), "access TTL");
    }

    public String generateAccessToken(String businessNumber, String name) {
        Instant now = Instant.now();
        long ttl = getAccessTtl();
        Instant exp = now.plusSeconds(ttl);

        return Jwts.builder()
                .subject(businessNumber)
                .claim("name", name)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public void validate(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }

    public String getBusinessNumber(String token) {
        Claims claims = parseClaimsOrThrow(token);
        return claims.getSubject();
    }

    public String getName(String token) {
        Claims claims = parseClaimsOrThrow(token);
        return claims.get("name", String.class);
    }

    private Claims parseClaimsOrThrow(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }
}
