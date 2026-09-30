package com.ridelink.account;

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
import com.ridelink.account.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AccountService accountService;

    private Account account;

    @BeforeEach
    void setUp() {

        account = new Account();

        account.setId("account-001");
        account.setName("Test User");
        account.setEmail("test@example.com");
        account.setPassword("encoded-password");
        account.setRole("PASSENGER");
        account.setStatus("ACTIVE");
    }

    // =========================================================
    // CREATE ACCOUNT
    // =========================================================

    @Test
    void createAccount_shouldCreatePassengerAccount() {

        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("Password123");

        when(accountRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encoded-password");

        when(accountRepository.save(any(Account.class)))
                .thenReturn(account);

        AccountResponse response =
                accountService.createAccount(request);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("PASSENGER", response.getRole());
        assertEquals("ACTIVE", response.getStatus());

        verify(passwordEncoder)
                .encode("Password123");

        verify(accountRepository)
                .save(any(Account.class));
    }

    // =========================================================
    // CREATE ACCOUNT - DUPLICATE EMAIL
    // =========================================================

    @Test
    void createAccount_shouldRejectDuplicateEmail() {

        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("Password123");

        when(accountRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(account));

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> accountService.createAccount(request)
        );

        verify(accountRepository, never())
                .save(any(Account.class));
    }

    // =========================================================
    // GET ACCOUNT
    // =========================================================

    @Test
    void getAccountById_shouldReturnAccount() {

        when(accountRepository.findById("account-001"))
                .thenReturn(Optional.of(account));

        AccountResponse response =
                accountService.getAccountById("account-001");

        assertNotNull(response);
        assertEquals("account-001", response.getId());
        assertEquals("Test User", response.getName());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("PASSENGER", response.getRole());
        assertEquals("ACTIVE", response.getStatus());
    }

    // =========================================================
    // GET ACCOUNT - NOT FOUND
    // =========================================================

    @Test
    void getAccountById_shouldThrowWhenNotFound() {

        when(accountRepository.findById("missing"))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.getAccountById("missing")
        );
    }

    // =========================================================
    // UPDATE PROFILE - OWNER
    // =========================================================

    @Test
    void updateAccount_shouldAllowAccountOwner() {

        UpdateAccountRequest request =
                new UpdateAccountRequest();
        request.setName("Updated User");
        request.setEmail("updated@example.com");

        when(accountRepository.findById("account-001"))
                .thenReturn(Optional.of(account));

        when(authentication.getName())
                .thenReturn("test@example.com");

        when(accountRepository.findByEmail("updated@example.com"))
                .thenReturn(Optional.empty());

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response =
                accountService.updateAccount(
                        "account-001",
                        request,
                        authentication
                );

        assertNotNull(response);
        assertEquals("Updated User", response.getName());
        assertEquals("updated@example.com", response.getEmail());

        verify(accountRepository)
                .save(account);
    }

    // =========================================================
    // UPDATE PROFILE - WRONG OWNER
    // =========================================================

    @Test
    void updateAccount_shouldRejectWrongOwner() {

        UpdateAccountRequest request =
                new UpdateAccountRequest();
        request.setName("Updated User");
        request.setEmail("updated@example.com");

        when(accountRepository.findById("account-001"))
                .thenReturn(Optional.of(account));

        when(authentication.getName())
                .thenReturn("another@example.com");

        assertThrows(
                AccessDeniedException.class,
                () -> accountService.updateAccount(
                        "account-001",
                        request,
                        authentication
                )
        );

        verify(accountRepository, never())
                .save(any(Account.class));
    }

    // =========================================================
    // UPDATE PROFILE - NO AUTHENTICATION
    // =========================================================

    @Test
    void updateAccount_shouldRejectMissingAuthentication() {

        UpdateAccountRequest request =
                new UpdateAccountRequest();
        request.setName("Updated User");
        request.setEmail("updated@example.com");

        when(accountRepository.findById("account-001"))
                .thenReturn(Optional.of(account));

        assertThrows(
                AccessDeniedException.class,
                () -> accountService.updateAccount(
                        "account-001",
                        request,
                        null
                )
        );

        verify(accountRepository, never())
                .save(any(Account.class));
    }

    // =========================================================
    // UPDATE PROFILE - DUPLICATE EMAIL
    // =========================================================

    @Test
    void updateAccount_shouldRejectDuplicateEmail() {

        UpdateAccountRequest request =
                new UpdateAccountRequest();
        request.setName("Updated User");
        request.setEmail("other@example.com");

        Account anotherAccount = new Account();
        anotherAccount.setId("account-002");
        anotherAccount.setEmail("other@example.com");

        when(accountRepository.findById("account-001"))
                .thenReturn(Optional.of(account));

        when(authentication.getName())
                .thenReturn("test@example.com");

        when(accountRepository.findByEmail("other@example.com"))
                .thenReturn(Optional.of(anotherAccount));

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> accountService.updateAccount(
                        "account-001",
                        request,
                        authentication
                )
        );

        verify(accountRepository, never())
                .save(any(Account.class));
    }

    // =========================================================
    // LOGIN - SUCCESS
    // =========================================================

    @Test
    void login_shouldReturnJwtToken() {

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123");

        when(accountRepository.findByEmail(
                "test@example.com"
        )).thenReturn(Optional.of(account));

        when(passwordEncoder.matches(
                "Password123",
                "encoded-password"
        )).thenReturn(true);

        when(jwtService.generateToken(
                "test@example.com",
                "PASSENGER"
        )).thenReturn("jwt-token");

        LoginResponse response =
                accountService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("PASSENGER", response.getRole());
    }

    // =========================================================
    // LOGIN - WRONG PASSWORD
    // =========================================================

    @Test
    void login_shouldRejectWrongPassword() {

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("WrongPassword");

        when(accountRepository.findByEmail(
                "test@example.com"
        )).thenReturn(Optional.of(account));

        when(passwordEncoder.matches(
                "WrongPassword",
                "encoded-password"
        )).thenReturn(false);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> accountService.login(request)
                );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );
    }

    // =========================================================
    // LOGIN - INACTIVE ACCOUNT
    // =========================================================

    @Test
    void login_shouldRejectInactiveAccount() {

        account.setStatus("INACTIVE");

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123");

        when(accountRepository.findByEmail(
                "test@example.com"
        )).thenReturn(Optional.of(account));

        when(passwordEncoder.matches(
                "Password123",
                "encoded-password"
        )).thenReturn(true);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> accountService.login(request)
                );

        assertEquals(
                "Account is not active",
                exception.getMessage()
        );
    }

    // =========================================================
    // UPDATE STATUS
    // =========================================================

    @Test
    void updateStatus_shouldUpdateStatus() {

        UpdateStatusRequest request =
                new UpdateStatusRequest("INACTIVE");

        when(accountRepository.findById("account-001"))
                .thenReturn(Optional.of(account));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response =
                accountService.updateStatus(
                        "account-001",
                        request
                );

        assertEquals("INACTIVE", response.getStatus());
        verify(accountRepository).save(account);
    }

    // =========================================================
    // UPDATE STATUS - INVALID
    // =========================================================

    @Test
    void updateStatus_shouldRejectInvalidStatus() {

        UpdateStatusRequest request =
                new UpdateStatusRequest("BLOCKED");

        when(accountRepository.findById("account-001"))
                .thenReturn(Optional.of(account));

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.updateStatus(
                        "account-001",
                        request
                )
        );

        verify(accountRepository, never())
                .save(any(Account.class));
    }

    // =========================================================
    // UPDATE ROLE
    // =========================================================

    @Test
    void updateRole_shouldUpdateRole() {

        UpdateRoleRequest request =
                new UpdateRoleRequest("DRIVER");

        when(accountRepository.findById("account-001"))
                .thenReturn(Optional.of(account));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response =
                accountService.updateRole(
                        "account-001",
                        request
                );

        assertEquals("DRIVER", response.getRole());
        verify(accountRepository).save(account);
    }

    // =========================================================
    // UPDATE ROLE - INVALID
    // =========================================================

    @Test
    void updateRole_shouldRejectInvalidRole() {

        UpdateRoleRequest request =
                new UpdateRoleRequest("SUPERUSER");

        when(accountRepository.findById("account-001"))
                .thenReturn(Optional.of(account));

        assertThrows(
                IllegalArgumentException.class,
                () -> accountService.updateRole(
                        "account-001",
                        request
                )
        );

        verify(accountRepository, never())
                .save(any(Account.class));
    }
}