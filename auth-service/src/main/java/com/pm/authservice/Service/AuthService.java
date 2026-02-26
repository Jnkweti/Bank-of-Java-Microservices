package com.pm.authservice.Service;

import com.pm.authservice.DTO.AuthResponseDTO;
import com.pm.authservice.DTO.LoginRequestDTO;
import com.pm.authservice.DTO.RegisterRequestDTO;
import com.pm.authservice.Enum.Role;
import com.pm.authservice.Exception.EmailAlreadyExistsException;
import com.pm.authservice.Exception.InvalidCredentialsException;
import com.pm.authservice.Exception.EmailNotFoundException;
import com.pm.authservice.Model.User;
import com.pm.authservice.Repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already registered: " + dto.getEmail());
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        // never store raw passwords, Remember BCrypt hashes + salts automatically
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        // everyone registers as CUSTOMER; ADMIN is assigned manually in the DB
        user.setRole(Role.CUSTOMER);

        userRepository.save(user);
        log.info("New user registered: {}", dto.getEmail());

        return new AuthResponseDTO(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user)
        );
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        // same error for "email not found" and "wrong password" don't tell which
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        log.info("User logged in: {}", dto.getEmail());

        return new AuthResponseDTO(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user)
        );
    }

    public AuthResponseDTO refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtService.validateToken(refreshToken);
        } catch (JwtException e) {
            // covers both expired and tampered tokens
            throw new InvalidCredentialsException("Refresh token is invalid or expired");
        }

        // reject access tokens submitted to the refresh endpoint
        String tokenType = claims.get("type", String.class);
        if (!"refresh".equals(tokenType)) {
            throw new InvalidCredentialsException("Token is not a refresh token");
        }

        Long userId = Long.parseLong(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("User no longer exists"));

        // only issue a new access token, refresh token keeps its original expiry
        return new AuthResponseDTO(
                jwtService.generateAccessToken(user),
                refreshToken
        );
    }
}
