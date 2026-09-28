package com.ridelink.account.service;

import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.LoginResponse;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.exception.AccountNotFoundException;
import com.ridelink.account.exception.EmailAlreadyExistsException;
import com.ridelink.account.model.Account;
import com.ridelink.account.repository.AccountRepository;
import com.ridelink.account.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AccountService(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AccountResponse createAccount(RegisterRequest request) {

        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(
                    "An account with this email already exists"
            );
        }

        Account account = new Account();

        account.setName(request.getName());
        account.setEmail(request.getEmail());
        account.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        account.setRole(request.getRole());
        account.setStatus(request.getStatus());

        Account savedAccount = accountRepository.save(account);

        return toAccountResponse(savedAccount);
    }

    public AccountResponse getAccountById(String id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with id: " + id
                        )
                );

        return toAccountResponse(account);
    }

    public LoginResponse login(LoginRequest request) {

        Account account = accountRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password")
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                account.getPassword()
        )) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new RuntimeException("Account is not active");
        }

        String token = jwtService.generateToken(
                account.getEmail(),
                account.getRole()
        );

        return new LoginResponse(
                token,
                account.getEmail(),
                account.getRole()
        );
    }

    private AccountResponse toAccountResponse(Account account) {

        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getEmail(),
                account.getRole(),
                account.getStatus()
        );
    }
}