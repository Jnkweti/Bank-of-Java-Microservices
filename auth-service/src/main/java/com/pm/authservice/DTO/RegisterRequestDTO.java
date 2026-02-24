package com.pm.authservice.DTO;


import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String email;
    private String password;
}
