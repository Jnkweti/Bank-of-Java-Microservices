package com.pm.accountservice.Controller;

import com.pm.accountservice.DTO.AccRequestDTO;
import com.pm.accountservice.DTO.AccResponseDTO;
import com.pm.accountservice.Service.accountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@AllArgsConstructor
public class AccountController {

    private final accountService accService;

    @PostMapping
    public ResponseEntity<AccResponseDTO> createAccount(@Valid @RequestBody AccRequestDTO accRequestDTO) {
        return ResponseEntity.ok(accService.createAccount(accRequestDTO));
    }
}