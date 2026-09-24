package com.ridelink.account;

import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.LoginResponse;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.exception.AccountNotFoundException;
import com.ridelink.account.exception.EmailAlreadyExistsException;
import com.ridelink.account.model.Account;
import com.ridelink.account.repository.AccountRepository;
import com.ridelink.account.security.JwtService;
import com.ridelink.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AccountService accountService;


    @Test
    void createAccount_shouldCreateAccountSuccessfully() {

        RegisterRequest request = new RegisterRequest();
        request.setName("John");
        request.setEmail("john@test.com");
        request.setPassword("Test123");
        request.setRole("PASSENGER");
        request.setStatus("ACTIVE");

        when(accountRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("Test123"))
                .thenReturn("hashedPassword");

        Account savedAccount = new Account(
                "John",
                "john@test.com",
                "hashedPassword",
                "PASSENGER",
                "ACTIVE"
        );

        savedAccount.setId("account123");

        when(accountRepository.save(any(Account.class)))
                .thenReturn(savedAccount);

        AccountResponse response = accountService.createAccount(request);

        assertNotNull(response);
        assertEquals("account123", response.getId());
        assertEquals("John", response.getName());
        assertEquals("john@test.com", response.getEmail());
        assertEquals("PASSENGER", response.getRole());
        assertEquals("ACTIVE", response.getStatus());

        verify(accountRepository).save(any(Account.class));
        verify(passwordEncoder).encode("Test123");
    }


    @Test
    void createAccount_shouldRejectDuplicateEmail() {

        RegisterRequest request = new RegisterRequest();
        request.setName("John");
        request.setEmail("john@test.com");
        request.setPassword("Test123");
        request.setRole("PASSENGER");
        request.setStatus("ACTIVE");

        Account existingAccount = new Account(
                "Existing User",
                "john@test.com",
                "hashedPassword",
                "PASSENGER",
                "ACTIVE"
        );

        when(accountRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(existingAccount));

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> accountService.createAccount(request)
        );

        verify(accountRepository, never()).save(any(Account.class));
    }


    @Test
    void getAccountById_shouldReturnAccountSuccessfully() {

        Account account = new Account(
                "John",
                "john@test.com",
                "hashedPassword",
                "PASSENGER",
                "ACTIVE"
        );

        account.setId("account123");

        when(accountRepository.findById("account123"))
                .thenReturn(Optional.of(account));

        AccountResponse response =
                accountService.getAccountById("account123");

        assertNotNull(response);
        assertEquals("account123", response.getId());
        assertEquals("John", response.getName());
        assertEquals("john@test.com", response.getEmail());
        assertEquals("PASSENGER", response.getRole());
        assertEquals("ACTIVE", response.getStatus());
    }


    @Test
    void getAccountById_shouldThrowExceptionWhenAccountNotFound() {

        when(accountRepository.findById("does-not-exist"))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.getAccountById("does-not-exist")
        );
    }


    @Test
    void login_shouldReturnJwtTokenForValidCredentials() {

        LoginRequest request = new LoginRequest();
        request.setEmail("john@test.com");
        request.setPassword("Test123");

        Account account = new Account(
                "John",
                "john@test.com",
                "hashedPassword",
                "PASSENGER",
                "ACTIVE"
        );

        when(accountRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(account));

        when(passwordEncoder.matches(
                "Test123",
                "hashedPassword"
        )).thenReturn(true);

        when(jwtService.generateToken(
                "john@test.com",
                "PASSENGER"
        )).thenReturn("mock-jwt-token");

        LoginResponse response = accountService.login(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("john@test.com", response.getEmail());
        assertEquals("PASSENGER", response.getRole());
    }


    @Test
    void login_shouldRejectInvalidPassword() {

        LoginRequest request = new LoginRequest();
        request.setEmail("john@test.com");
        request.setPassword("WrongPassword");

        Account account = new Account(
                "John",
                "john@test.com",
                "hashedPassword",
                "PASSENGER",
                "ACTIVE"
        );

        when(accountRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(account));

        when(passwordEncoder.matches(
                "WrongPassword",
                "hashedPassword"
        )).thenReturn(false);

        assertThrows(
                RuntimeException.class,
                () -> accountService.login(request)
        );

        verify(jwtService, never())
                .generateToken(anyString(), anyString());
    }


    @Test
    void login_shouldRejectInactiveAccount() {

        LoginRequest request = new LoginRequest();
        request.setEmail("john@test.com");
        request.setPassword("Test123");

        Account account = new Account(
                "John",
                "john@test.com",
                "hashedPassword",
                "PASSENGER",
                "INACTIVE"
        );

        when(accountRepository.findByEmail("john@test.com"))
                .thenReturn(Optional.of(account));

        when(passwordEncoder.matches(
                "Test123",
                "hashedPassword"
        )).thenReturn(true);

        assertThrows(
                RuntimeException.class,
                () -> accountService.login(request)
        );

        verify(jwtService, never())
                .generateToken(anyString(), anyString());
    }
}