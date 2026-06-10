package com.bachelor.service_desk.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.secret-encoding:plain}")
    private String secretEncoding;

    @Value("${jwt.expiration:86400000}") // 24 hours default
    private Long expiration;

    @Value("${jwt.refresh-expiration:604800000}") // 7 days default
    private Long refreshExpiration;

    private SecretKey signingKey;

    @PostConstruct
    void init() {
        validateDurations();
        signingKey = buildSigningKey();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Login from token (numberPhone claim or subject fallback). */
    public String extractLogin(String token) {
        Claims claims = extractAllClaims(token);
        String numberPhone = claims.get("numberPhone", String.class);
        if (numberPhone != null && !numberPhone.isBlank()) {
            return numberPhone;
        }
        return claims.getSubject();
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object value = claims.get("userId");
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails, Long userId) {
        return generateAccessToken(userDetails, userId);
    }

    public String generateAccessToken(UserDetails userDetails, Long userId) {
        String numberPhone = userDetails.getUsername();
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("numberPhone", numberPhone);
        claims.put("tokenType", "ACCESS");
        claims.put("roles", extractRoles(userDetails));
        return createToken(claims, numberPhone, expiration);
    }

    public String generateRefreshToken(UserDetails userDetails, Long userId) {
        String numberPhone = userDetails.getUsername();
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("numberPhone", numberPhone);
        claims.put("tokenType", "REFRESH");
        claims.put("jti", UUID.randomUUID().toString());
        return createToken(claims, numberPhone, refreshExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long ttlMs) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ttlMs))
                .signWith(getSigningKey())
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Boolean validateRefreshToken(String token, UserDetails userDetails) {
        return validateToken(token, userDetails) && "REFRESH".equals(extractTokenType(token));
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("tokenType", String.class);
    }

    public Instant extractExpirationInstant(String token) {
        return extractExpiration(token).toInstant();
    }

    private List<String> extractRoles(UserDetails userDetails) {
        return userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    private SecretKey getSigningKey() {
        return signingKey;
    }

    private void validateDurations() {
        if (expiration == null || expiration <= 0) {
            throw new IllegalArgumentException("jwt.expiration must be greater than 0");
        }
        if (refreshExpiration == null || refreshExpiration <= 0) {
            throw new IllegalArgumentException("jwt.refresh-expiration must be greater than 0");
        }
    }

    private SecretKey buildSigningKey() {
        byte[] keyBytes = decodeSecret();
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 256 bits (32 bytes) for HS256");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] decodeSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("jwt.secret must not be blank");
        }

        return switch (secretEncoding.toLowerCase()) {
            case "auto" -> decodeAutoSecret();
            case "hex" -> decodeHexSecret();
            case "base64" -> Base64.getDecoder().decode(secret);
            case "plain", "raw" -> secret.getBytes(StandardCharsets.UTF_8);
            default -> throw new IllegalArgumentException(
                    "Unsupported jwt.secret-encoding: " + secretEncoding + ". Use auto, plain, raw, hex or base64"
            );
        };
    }

    private byte[] decodeAutoSecret() {
        if (looksLikeHex(secret)) {
            return decodeHexSecret();
        }

        byte[] base64Bytes = tryDecodeBase64(secret, Base64.getDecoder());
        if (base64Bytes != null) {
            return base64Bytes;
        }

        byte[] base64UrlBytes = tryDecodeBase64(secret, Base64.getUrlDecoder());
        if (base64UrlBytes != null) {
            return base64UrlBytes;
        }

        return secret.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] decodeHexSecret() {
        try {
            return HexFormat.of().parseHex(secret);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("jwt.secret is not valid hex", ex);
        }
    }

    private boolean looksLikeHex(String value) {
        return value.length() % 2 == 0 && value.matches("[0-9a-fA-F]+");
    }

    private byte[] tryDecodeBase64(String value, Decoder decoder) {
        try {
            byte[] decoded = decoder.decode(value);
            return decoded.length >= 32 ? decoded : null;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
