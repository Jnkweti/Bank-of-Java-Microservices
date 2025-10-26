package com.pm.customerservice.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
@Data
public class customerRequestDTO {

    @NotBlank(message= "first name required")
    @Size(max = 100, message="first name cannot exceed 100 characters")
    private String firstName;

    @NotBlank(message = "last name required")
    @Size(max = 100, message="last name cannot exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Email required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Date of birth required")
    private String birthDate;

    private LocalDate registerDate;

}
