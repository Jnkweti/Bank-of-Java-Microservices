package com.pm.customerservice.Mapper;

import com.pm.customerservice.DTO.customerRequestDTO;
import com.pm.customerservice.DTO.customerResponseDTO;
import com.pm.customerservice.mapper.Mapper;
import com.pm.customerservice.model.customer;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


public class MapperTest {

    @Test
    void toEntity_shouldMapAllFieldsCorrectly() {
        customerRequestDTO dto = new customerRequestDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setAddress("123 Main ST.");
        dto.setBirthDate("1990-05-15");

        customer result = Mapper.toEntity(dto);

        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getAddress()).isEqualTo("123 Main ST.");
        assertThat(result.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(result.getRegisterDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void toEntity_shouldThrowWhenDateFormatIsInvalid() {
        customerRequestDTO dto = new customerRequestDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setAddress("123 Main ST.");
        dto.setBirthDate("not-a-date");

        assertThatThrownBy(() -> Mapper.toEntity(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Birth date must be in format YYYY-MM-DD");
    }

    @Test
    void toDTO_shouldMapAllFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        customer c = new customer();
        c.setId(id);
        c.setFirstName("Jane");
        c.setLastName("Smith");
        c.setEmail("jane@example.com");
        c.setAddress("456 Oak Ave");
        c.setBirthDate(LocalDate.of(1985, 3, 20));

        customerResponseDTO result = Mapper.toDTO(c);

        assertThat(result.getId()).isEqualTo(id.toString());
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getEmail()).isEqualTo("jane@example.com");
        assertThat(result.getAddress()).isEqualTo("456 Oak Ave");
        assertThat(result.getBirthDate()).isEqualTo("1985-03-20");
    }
}
