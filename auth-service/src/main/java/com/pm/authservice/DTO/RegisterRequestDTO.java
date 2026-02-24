package com.pm.authservice.DTO;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank
    @Email
    private String email;

    // min 8 chars — enforced here so we don't even BCrypt garbage input
    @NotBlank
    @Size(min = 8, message = "password must be at least 8 characters")
    private String password;
}
