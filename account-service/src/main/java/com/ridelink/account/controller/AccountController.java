package com.ridelink.account.controller;

import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.LoginResponse;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.dto.UpdateAccountRequest;
import com.ridelink.account.dto.UpdateRoleRequest;
import com.ridelink.account.dto.UpdateStatusRequest;
import com.ridelink.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/accounts")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // =========================
    // CREATE ACCOUNT
    // =========================
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(
            @Valid @RequestBody RegisterRequest request
    ) {
        return accountService.createAccount(request);
    }

    // =========================
    // GET ACCOUNT
    // =========================
    @GetMapping("/{id}")
    public AccountResponse getAccountById(
            @PathVariable String id
    ) {
        return accountService.getAccountById(id);
    }

    // =========================
    // UPDATE PROFILE
    // =========================
    @PutMapping("/{id}")
    public AccountResponse updateAccount(
            @PathVariable String id,
            @Valid @RequestBody UpdateAccountRequest request
    ) {
        return accountService.updateAccount(id, request);
    }

    // =========================
    // UPDATE ACCOUNT STATUS
    // =========================
    @PatchMapping("/{id}/status")
    public AccountResponse updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        return accountService.updateStatus(id, request);
    }

    // =========================
    // UPDATE ACCOUNT ROLE
    // =========================
    @PatchMapping("/{id}/role")
    public AccountResponse updateRole(
            @PathVariable String id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        return accountService.updateRole(id, request);
    }

    // =========================
    // LOGIN
    // =========================
    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return accountService.login(request);
    }

    // =========================
    // ADMIN TEST
    // =========================
    @GetMapping("/admin/test")
    public String adminTest() {
        return "Admin access granted";
    }
}