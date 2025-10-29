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

    public @NotBlank(message = "first name required") @Size(max = 100, message = "first name cannot exceed 100 characters") String getFirstName() {
        return firstName;
    }

    public void setFirstName(@NotBlank(message = "first name required") @Size(max = 100, message = "first name cannot exceed 100 characters") String firstName) {
        this.firstName = firstName;
    }

    public @NotBlank(message = "last name required") @Size(max = 100, message = "last name cannot exceed 100 characters") String getLastName() {
        return lastName;
    }

    public void setLastName(@NotBlank(message = "last name required") @Size(max = 100, message = "last name cannot exceed 100 characters") String lastName) {
        this.lastName = lastName;
    }

    public @NotBlank(message = "Address is required") String getAddress() {
        return address;
    }

    public void setAddress(@NotBlank(message = "Address is required") String address) {
        this.address = address;
    }

    public @NotBlank(message = "Email required") @Email(message = "Email should be valid") String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank(message = "Email required") @Email(message = "Email should be valid") String email) {
        this.email = email;
    }

    public @NotBlank(message = "Date of birth required") String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(@NotBlank(message = "Date of birth required") String birthDate) {
        this.birthDate = birthDate;
    }



}
