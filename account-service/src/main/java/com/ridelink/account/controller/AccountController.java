package com.ridelink.account.controller;

import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.LoginResponse;
import com.ridelink.account.model.Account;
import com.ridelink.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@RequestBody Account account) {
        return accountService.createAccount(account);
    }

    @GetMapping("/{id}")
    public AccountResponse getAccountById(@PathVariable String id) {
        return accountService.getAccountById(id);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return accountService.login(request);
    }
}