package com.pm.accountservice.Controller;

import com.pm.accountservice.DTO.AccRequestDTO;
import com.pm.accountservice.DTO.AccResponseDTO;
import com.pm.accountservice.Service.accountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@AllArgsConstructor
public class AccountController {

    private final accountService accService;

    @GetMapping
    public ResponseEntity<List<AccResponseDTO>> getAllAccounts() {
        return ResponseEntity.ok(accService.getAllAccounts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccResponseDTO> getAccount(@PathVariable String id) {
        return ResponseEntity.ok(accService.getAccount(id));
    }

    @PostMapping
    public ResponseEntity<AccResponseDTO> createAccount(@Valid @RequestBody AccRequestDTO accRequestDTO) {
        return ResponseEntity.ok(accService.createAccount(accRequestDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccResponseDTO> updateAccount(@PathVariable String id,
                                                        @Valid @RequestBody AccRequestDTO accRequestDTO) {
        return ResponseEntity.ok(accService.updateAccount(id, accRequestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAccount(@PathVariable String id) {
        accService.deleteAccount(id);
        return ResponseEntity.ok("Account has been deleted!");
    }
}
