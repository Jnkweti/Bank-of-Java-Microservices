package com.pm.apigateway.Controller;

import com.pm.proto.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountGatewayController {

    @GrpcClient("account-service")
    private AccountServGrpc.AccountServBlockingStub stub;

    private Map<String, String> accountToMap(Account account) {
        return Map.ofEntries(
                Map.entry("id", account.getId()),
                Map.entry("customerId", account.getCustomerId()),
                Map.entry("accName", account.getAccName()),
                Map.entry("type", account.getType().name()),
                Map.entry("status", account.getStatus().name()),
                Map.entry("balance", account.getBalance()),
                Map.entry("accountNumber", account.getAccountNumber()),
                Map.entry("openedDate", account.getOpenedDate()),
                Map.entry("lastUpdated", account.getLastUpdated()),
                Map.entry("interestRate", account.getInterestRate())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, String>> getAccountById(@PathVariable String id) {
        GetAccIdRequest request = GetAccIdRequest.newBuilder().setId(id).build();
        GetAccIDResponse response = stub.getAccById(request);
        return ResponseEntity.ok(accountToMap(response.getAccount()));
    }


    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Map<String, String>>> getAccountByCustomerId(@PathVariable String customerId) {
        GetAccByCusIdRequest request = GetAccByCusIdRequest.newBuilder().setCusId(customerId).build();
        GetAccByCusIdResponse response = stub.getAccByCusId(request);
        List<Map<String, String>> accounts = response.getAccountList().stream()
                .map(this::accountToMap)
                .toList();
        return ResponseEntity.ok(accounts);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createAccount(@RequestBody Map<String, String> body) {
        CreateAccRequest request = CreateAccRequest.newBuilder()
                .setAccName(body.get("accName"))
                .setCustomerId(body.get("customerId"))
                .setType(AccType.valueOf(body.get("type")))
                .setStatus(AccStatus.valueOf(body.get("status")))
                .setBalance(body.get("balance"))
                .build();

        CreateAccResponse response = stub.createAcc(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountToMap(response.getAccount()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateAccount(@PathVariable String id,
                                                              @RequestBody Map<String, String> body) {
        UpdateAccRequest request = UpdateAccRequest.newBuilder()
                .setId(id)
                .setAccName(body.get("accName"))
                .setType(AccType.valueOf(body.get("type")))
                .setStat(AccStatus.valueOf(body.get("status")))
                .setBalance(body.get("balance"))
                .build();

        UpdateAccResponse response = stub.updateAcc(request);
        return ResponseEntity.ok(accountToMap(response.getAccount()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAccount(@PathVariable String id) {
        DeleteAccByIdRequest request = DeleteAccByIdRequest.newBuilder().setAccId(id).build();
        DeleteAccByIdResponse response = stub.deleteAccIdRequest(request);
        return ResponseEntity.ok(Map.of("status", response.getStatus()));
    }
}
