package com.pm.customerservice.DTO;

import lombok.Data;

@Data
public class customerResponseDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String birthDate;
}
