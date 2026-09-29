package com.ridelink.account.service;

import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.LoginResponse;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.dto.UpdateAccountRequest;
import com.ridelink.account.dto.UpdateRoleRequest;
import com.ridelink.account.dto.UpdateStatusRequest;
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

    // =========================
    // CREATE ACCOUNT
    // =========================
    public AccountResponse createAccount(RegisterRequest request) {

        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(
                    "An account with this email already exists"
            );
        }

        Account account = new Account();

        account.setName(request.getName());
        account.setEmail(request.getEmail());

        // Always hash the password before saving
        account.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        // Public registration cannot create ADMIN accounts
        account.setRole("PASSENGER");

        // New accounts are active by default
        account.setStatus("ACTIVE");

        Account savedAccount = accountRepository.save(account);

        return toAccountResponse(savedAccount);
    }

    // =========================
    // GET ACCOUNT
    // =========================
    public AccountResponse getAccountById(String id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with id: " + id
                        )
                );

        return toAccountResponse(account);
    }

    // =========================
    // UPDATE PROFILE
    // =========================
    public AccountResponse updateAccount(
            String id,
            UpdateAccountRequest request
    ) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with id: " + id
                        )
                );

        // Check whether the new email already belongs to another account
        if (!account.getEmail().equalsIgnoreCase(request.getEmail())) {

            if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new EmailAlreadyExistsException(
                        "An account with this email already exists"
                );
            }
        }

        account.setName(request.getName());
        account.setEmail(request.getEmail());

        Account updatedAccount = accountRepository.save(account);

        return toAccountResponse(updatedAccount);
    }

    // =========================
    // LOGIN
    // =========================
    public LoginResponse login(LoginRequest request) {

        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid email or password"
                        )
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                account.getPassword()
        )) {
            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new RuntimeException(
                    "Account is not active"
            );
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

    // =========================
    // UPDATE ACCOUNT STATUS
    // =========================
    public AccountResponse updateStatus(
            String id,
            UpdateStatusRequest request
    ) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with id: " + id
                        )
                );

        String newStatus = request.getStatus().toUpperCase();

        if (!newStatus.equals("ACTIVE")
                && !newStatus.equals("INACTIVE")) {

            throw new IllegalArgumentException(
                    "Status must be ACTIVE or INACTIVE"
            );
        }

        account.setStatus(newStatus);

        Account updatedAccount = accountRepository.save(account);

        return toAccountResponse(updatedAccount);
    }

    // =========================
    // UPDATE ACCOUNT ROLE
    // =========================
    public AccountResponse updateRole(
            String id,
            UpdateRoleRequest request
    ) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with id: " + id
                        )
                );

        String newRole = request.getRole().toUpperCase();

        if (!newRole.equals("PASSENGER")
                && !newRole.equals("DRIVER")
                && !newRole.equals("ADMIN")) {

            throw new IllegalArgumentException(
                    "Role must be PASSENGER, DRIVER or ADMIN"
            );
        }

        account.setRole(newRole);

        Account updatedAccount = accountRepository.save(account);

        return toAccountResponse(updatedAccount);
    }

    // =========================
    // CONVERT ACCOUNT TO RESPONSE
    // =========================
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