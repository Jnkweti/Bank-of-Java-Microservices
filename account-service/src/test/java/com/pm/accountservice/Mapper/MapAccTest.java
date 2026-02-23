package com.pm.accountservice.Mapper;

import com.pm.accountservice.DTO.AccRequestDTO;
import com.pm.accountservice.DTO.AccResponseDTO;
import com.pm.accountservice.Enum.AccountStatus;
import com.pm.accountservice.Enum.AccountType;
import com.pm.accountservice.model.account;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Pure unit test - no Spring context needed, no mocks needed.
// The mapper is a static utility class so we just call its methods directly.
class MapAccTest {

    // Helper to build a valid request DTO so each test stays focused
    private AccRequestDTO buildRequestDTO(AccountType type) {
        AccRequestDTO dto = new AccRequestDTO();
        dto.setAccountName("John's Savings");
        dto.setCustomerId(UUID.randomUUID().toString());
        dto.setBalance("500.00");
        dto.setType(type);
        dto.setStatus(AccountStatus.ACTIVE);
        return dto;
    }

    // Helper to build a fully populated account entity
    private account buildAccount() {
        account acc = new account();
        acc.setId(UUID.randomUUID());
        acc.setAccountName("John's Savings");
        acc.setAccountNumber("BOJ-0000000001");
        acc.setCustomerId(UUID.randomUUID());
        acc.setBalance(new BigDecimal("500.00"));
        acc.setAccountType(AccountType.SAVINGS);
        acc.setStatus(AccountStatus.ACTIVE);
        acc.setInterestRate(new BigDecimal("0.045"));
        acc.setLastUpdated(LocalDateTime.now());
        return acc;
    }

    // --- toEntity tests ---

    @Test
    void toEntity_shouldMapAllBaseFieldsCorrectly() {
        AccRequestDTO dto = buildRequestDTO(AccountType.CHECKING);

        account result = MapAcc.toEntity(dto);

        assertThat(result.getAccountName()).isEqualTo("John's Savings");
        assertThat(result.getCustomerId()).isEqualTo(UUID.fromString(dto.getCustomerId()));
        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(result.getAccountType()).isEqualTo(AccountType.CHECKING);
        assertThat(result.getLastUpdated()).isNotNull();
    }

    @Test
    void toEntity_shouldSetSavingsInterestRate() {
        // SAVINGS accounts get a 4.5% interest rate
        account result = MapAcc.toEntity(buildRequestDTO(AccountType.SAVINGS));
        assertThat(result.getInterestRate()).isEqualByComparingTo(new BigDecimal("0.045"));
    }

    @Test
    void toEntity_shouldSetLoanInterestRate() {
        // LOAN accounts get a 0.45% interest rate
        account result = MapAcc.toEntity(buildRequestDTO(AccountType.LOAN));
        assertThat(result.getInterestRate()).isEqualByComparingTo(new BigDecimal("0.0045"));
    }

    @Test
    void toEntity_shouldSetCreditInterestRate() {
        // CREDIT accounts also get 0.45%
        account result = MapAcc.toEntity(buildRequestDTO(AccountType.CREDIT));
        assertThat(result.getInterestRate()).isEqualByComparingTo(new BigDecimal("0.0045"));
    }

    @Test
    void toEntity_shouldSetZeroInterestRateForChecking() {
        // CHECKING accounts earn no interest
        account result = MapAcc.toEntity(buildRequestDTO(AccountType.CHECKING));
        assertThat(result.getInterestRate()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void toEntity_shouldThrowWhenBalanceIsNotANumber() {
        // balance is a String in the DTO - if it's not numeric BigDecimal throws
        AccRequestDTO dto = buildRequestDTO(AccountType.SAVINGS);
        dto.setBalance("not-a-number");

        // NumberFormatException is what BigDecimal throws on invalid input
        assertThatThrownBy(() -> MapAcc.toEntity(dto))
                .isInstanceOf(NumberFormatException.class);
    }

    // --- toDTO tests ---

    @Test
    void toDTO_shouldMapAllFieldsCorrectly() {
        account acc = buildAccount();

        AccResponseDTO result = MapAcc.toDTO(acc);

        assertThat(result.getAccountId()).isEqualTo(acc.getId().toString());
        assertThat(result.getAccountName()).isEqualTo("John's Savings");
        assertThat(result.getAccountNumber()).isEqualTo("BOJ-0000000001");
        assertThat(result.getAccountBalance()).isEqualTo("500.00");
        assertThat(result.getCustomerId()).isEqualTo(acc.getCustomerId().toString());
        assertThat(result.getAccountType()).isEqualTo("SAVINGS");
        assertThat(result.getAccountStatus()).isEqualTo("ACTIVE");
        assertThat(result.getInterestRate()).isEqualTo("0.045");
        assertThat(result.getLastUpdate()).isNotNull();
    }
}
