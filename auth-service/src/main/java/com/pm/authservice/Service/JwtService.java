package com.pm.authservice.Service;

import com.pm.authservice.Model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    @Value("${jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    // generated once on startup, held in memory for the life of the process
    private PrivateKey privateKey;
    private PublicKey  publicKey;

    // runs after Spring injects the @Value fields, before we serve any requests
    @PostConstruct
    public void generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();

            this.privateKey = pair.getPrivate();
            this.publicKey  = pair.getPublic();

            log.info("RSA-2048 key pair ready");
        } catch (Exception e) {
            // no point starting up if we can't sign tokens
            throw new IllegalStateException("Failed to generate RSA key pair", e);
        }
    }

    // short-lived (15 min) — carries email + role so the gateway doesn't need a DB call
    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiryMs))
                .signWith(privateKey)
                .compact();
    }

    // long-lived (7 days) — kept minimal intentionally, only used at /auth/refresh
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiryMs))
                .signWith(privateKey)
                .compact();
    }

    // throws ExpiredJwtException or JwtException if anything's wrong — let the caller handle it
    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // gateway fetches this once at startup via /.well-known/jwks.json
    public PublicKey getPublicKey() {
        return publicKey;
    }
}
