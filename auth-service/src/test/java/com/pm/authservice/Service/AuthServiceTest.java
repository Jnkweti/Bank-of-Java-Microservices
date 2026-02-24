package com.pm.authservice.Service;

import com.pm.authservice.DTO.AuthResponseDTO;
import com.pm.authservice.DTO.LoginRequestDTO;
import com.pm.authservice.DTO.RegisterRequestDTO;
import com.pm.authservice.Enum.Role;
import com.pm.authservice.Exception.EmailAlreadyExistsException;
import com.pm.authservice.Exception.InvalidCredentialsException;
import com.pm.authservice.Model.User;
import com.pm.authservice.Repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks private AuthService authService;

    // ── register ─────────────────────────────────────────────────────────────

    @Test
    void register_shouldSaveUserAndReturnTokens_whenEmailIsNew() {
        when(userRepository.findByEmail("alice@bank.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateAccessToken(any())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-token");

        AuthResponseDTO response = authService.register(buildRegisterRequest("alice@bank.com", "password123"));

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        verify(userRepository).save(argThat(u ->
                u.getEmail().equals("alice@bank.com") &&
                u.getPasswordHash().equals("hashed") &&
                u.getRole() == Role.CUSTOMER
        ));
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        when(userRepository.findByEmail("alice@bank.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(buildRegisterRequest("alice@bank.com", "password123")))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid() {
        User user = buildUser(1L, "alice@bank.com", "hashed");
        when(userRepository.findByEmail("alice@bank.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

        AuthResponseDTO response = authService.login(buildLoginRequest("alice@bank.com", "password123"));

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void login_shouldThrow_whenEmailNotFound() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(buildLoginRequest("nobody@bank.com", "pass")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void login_shouldThrow_whenPasswordIsWrong() {
        User user = buildUser(1L, "alice@bank.com", "hashed");
        when(userRepository.findByEmail("alice@bank.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(buildLoginRequest("alice@bank.com", "wrongpass")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    // ── refresh ───────────────────────────────────────────────────────────────

    @Test
    void refresh_shouldReturnNewAccessToken_whenTokenIsValid() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("1");
        when(jwtService.validateToken("valid-refresh")).thenReturn(claims);

        User user = buildUser(1L, "alice@bank.com", "hashed");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");

        AuthResponseDTO response = authService.refresh("valid-refresh");

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        // refresh token is passed back unchanged
        assertThat(response.getRefreshToken()).isEqualTo("valid-refresh");
    }

    @Test
    void refresh_shouldThrow_whenTokenIsInvalid() {
        when(jwtService.validateToken("bad-token")).thenThrow(new JwtException("expired"));

        assertThatThrownBy(() -> authService.refresh("bad-token"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("invalid or expired");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private RegisterRequestDTO buildRegisterRequest(String email, String password) {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    private LoginRequestDTO buildLoginRequest(String email, String password) {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    private User buildUser(Long id, String email, String hash) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPasswordHash(hash);
        user.setRole(Role.CUSTOMER);
        return user;
    }
}
