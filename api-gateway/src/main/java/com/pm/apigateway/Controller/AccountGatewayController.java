package com.pm.apigateway.Controller;

import com.pm.proto.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountGatewayController {

    @GrpcClient("account-service")
    private AccountServGrpc.AccountServBlockingStub stub;

    // helper so we're not duplicating the same 6-field map in every method
    private Map<String, String> accountToMap(Account account) {
        return Map.of(
                "id", account.getId(),
                "customerId", account.getCustomerId(),
                "accName", account.getAccName(),
                "type", account.getType().name(),
                "status", account.getStatus().name(),
                "balance", account.getBalance()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, String>> getAccountById(@PathVariable String id) {
        GetAccIdRequest request = GetAccIdRequest.newBuilder().setId(id).build();
        GetAccIDResponse response = stub.getAccById(request);
        return ResponseEntity.ok(accountToMap(response.getAccount()));
    }

    // both GetAccById and GetAccByCusId use GetAccIdRequest in the proto —
    // we pass the customerId as the id field
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Map<String, String>> getAccountByCustomerId(@PathVariable String customerId) {
        GetAccIdRequest request = GetAccIdRequest.newBuilder().setId(customerId).build();
        GetAccIDResponse response = stub.getAccByCusId(request);
        return ResponseEntity.ok(accountToMap(response.getAccount()));
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
        // note: the proto field for status in UpdateAccRequest is "stat" not "status"
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
