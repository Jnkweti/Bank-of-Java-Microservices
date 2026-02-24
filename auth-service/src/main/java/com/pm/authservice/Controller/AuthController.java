package com.pm.authservice.Controller;

import com.pm.authservice.DTO.AuthResponseDTO;
import com.pm.authservice.DTO.LoginRequestDTO;
import com.pm.authservice.DTO.RegisterRequestDTO;
import com.pm.authservice.Service.AuthService;
import com.pm.authservice.Service.JwtService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    // client sends the refresh token in the request body, gets back a new access token
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    // JWKS endpoint — the gateway fetches this once at startup to get our public key.
    // Returns the key in JWK format (standard JSON structure that JWT libraries understand).
    // RSAPublicKey gives us the modulus (n) and exponent (e) needed to reconstruct the key.
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> jwks() {
        RSAPublicKey rsaKey = (RSAPublicKey) jwtService.getPublicKey();

        Map<String, Object> jwk = Map.of(
                "kty", "RSA",
                "alg", "RS256",
                "use", "sig",
                "n", Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(rsaKey.getModulus().toByteArray()),
                "e", Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(rsaKey.getPublicExponent().toByteArray())
        );

        return ResponseEntity.ok(Map.of("keys", new Object[]{jwk}));
    }
}
