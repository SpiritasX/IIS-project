package com.example.iis.service;

import com.example.iis.dto.LoginRequest;
import com.example.iis.dto.SignupRequest;
import com.example.iis.model.Admin;
import com.example.iis.model.Botanist;
import com.example.iis.model.Customer;
import com.example.iis.model.Worker;
import com.example.iis.repository.AccountRepository;
import com.example.iis.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(customerRepository, accountRepository, passwordEncoder);
    }

    @Test
    void signupHashesPasswordAndNormalizesEmail() {
        when(accountRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(accountRepository.existsByUsername("user")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed-secret");
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.signup(new SignupRequest("USER@example.com", "secret"));

        assertEquals("user@example.com", response.email());
        assertEquals("user Customer", response.name());

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        assertEquals("hashed-secret", customerCaptor.getValue().getPassword());
    }

    @Test
    void loginRejectsInvalidPassword() {
        Customer customer = new Customer("user", "hashed-secret", "User", "Customer", "user@example.com");
        when(accountRepository.findByEmail("user@example.com")).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("wrong", "hashed-secret")).thenReturn(false);

        assertThrows(
                ResponseStatusException.class,
                () -> authService.login(new LoginRequest("user@example.com", "wrong"))
        );
    }

    @Test
    void loginReturnsRoleForStaffAccounts() {
        when(passwordEncoder.matches("secret", "hashed-admin")).thenReturn(true);
        when(passwordEncoder.matches("secret", "hashed-botanist")).thenReturn(true);
        when(passwordEncoder.matches("secret", "hashed-worker")).thenReturn(true);
        when(accountRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(new Admin("admin", "hashed-admin", "Admin", "User", "admin@example.com")));
        when(accountRepository.findByEmail("botanist@example.com"))
                .thenReturn(Optional.of(new Botanist(
                        "botanist",
                        "hashed-botanist",
                        "Botanist",
                        "User",
                        "botanist@example.com"
                )));
        when(accountRepository.findByEmail("worker@example.com"))
                .thenReturn(Optional.of(new Worker("worker", "hashed-worker", "Worker", "User", "worker@example.com")));

        assertEquals("ADMIN", authService.login(new LoginRequest("admin@example.com", "secret")).role());
        assertEquals("BOTANIST", authService.login(new LoginRequest("botanist@example.com", "secret")).role());
        assertEquals("WORKER", authService.login(new LoginRequest("worker@example.com", "secret")).role());
    }
}
