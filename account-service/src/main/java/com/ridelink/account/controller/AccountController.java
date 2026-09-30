package com.ridelink.account.controller;

import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.LoginResponse;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.dto.UpdateAccountRequest;
import com.ridelink.account.dto.UpdateRoleRequest;
import com.ridelink.account.dto.UpdateStatusRequest;
import com.ridelink.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @Operation(
            summary = "Create account",
            description = "Creates a new passenger account"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created"),
            @ApiResponse(responseCode = "400", description = "Invalid account data"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(request));
    }

    // =========================
    // LOGIN
    // =========================

    @Operation(
            summary = "Login",
            description = "Authenticates an account and returns a JWT"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(accountService.login(request));
    }

    // =========================
    // GET ACCOUNT
    // =========================

    @Operation(
            summary = "Get account",
            description = "Retrieves an account by ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    // =========================
    // UPDATE PROFILE
    // =========================

    @Operation(
            summary = "Update own profile",
            description = "Allows an authenticated user to update their own name and email"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "403", description = "User is not the account owner"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable String id,
            @Valid @RequestBody UpdateAccountRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                accountService.updateAccount(id, request, authentication)
        );
    }

    // =========================
    // UPDATE STATUS - ADMIN
    // =========================

    @Operation(
            summary = "Update account status",
            description = "Allows an administrator to activate or deactivate an account"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "403", description = "Admin role required"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<AccountResponse> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        return ResponseEntity.ok(
                accountService.updateStatus(id, request)
        );
    }

    // =========================
    // UPDATE ROLE - ADMIN
    // =========================

    @Operation(
            summary = "Update account role",
            description = "Allows an administrator to change an account role"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role updated"),
            @ApiResponse(responseCode = "400", description = "Invalid role"),
            @ApiResponse(responseCode = "403", description = "Admin role required"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PatchMapping("/{id}/role")
    public ResponseEntity<AccountResponse> updateRole(
            @PathVariable String id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        return ResponseEntity.ok(
                accountService.updateRole(id, request)
        );
    }
}