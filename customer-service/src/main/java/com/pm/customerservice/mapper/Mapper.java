package com.pm.customerservice.mapper;

import com.pm.customerservice.DTO.customerRequestDTO;
import com.pm.customerservice.DTO.customerResponseDTO;
import com.pm.customerservice.Repo.customerRepo;
import com.pm.customerservice.model.customer;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public class Mapper {

    public static customerResponseDTO toDTO(customer customer){
        customerResponseDTO dto = new customerResponseDTO();
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setEmail(customer.getEmail());
        dto.setAddress(customer.getAddress());
        dto.setBirthDate(customer.getBirthDate().toString());
        return dto;
    }

    public static customer toEntity(customerRequestDTO dto){
        customer entity = new customer();
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setAddress(dto.getAddress());
        entity.setBirthDate(LocalDate.parse(dto.getBirthDate()));
        entity.setRegisterDate(LocalDate.now());
        return entity;



    }
}
