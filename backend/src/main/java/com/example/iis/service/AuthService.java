package com.example.iis.service;

import com.example.iis.dto.AddressUpdateRequest;
import com.example.iis.dto.AuthUserResponse;
import com.example.iis.dto.EmailUpdateRequest;
import com.example.iis.dto.LoginRequest;
import com.example.iis.dto.PasswordUpdateRequest;
import com.example.iis.dto.PersonalDataUpdateRequest;
import com.example.iis.dto.SignupRequest;
import com.example.iis.model.Account;
import com.example.iis.model.Customer;
import com.example.iis.repository.AccountRepository;
import com.example.iis.repository.CustomerRepository;
import com.example.iis.security.AccountPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.stream.Stream;

@Service
public class AuthService {
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthUserResponse signup(SignupRequest request) {
        String email = normalizeEmail(request == null ? null : request.email());
        String password = normalizePassword(request == null ? null : request.password());

        if (accountRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        String username = uniqueUsernameFor(email);
        String displayName = displayNameFor(email);
        Customer customer = new Customer(
                username,
                passwordEncoder.encode(password),
                displayName,
                "Customer",
                email
        );

        return toResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public AuthUserResponse currentUser(AccountPrincipal principal) {
        if (principal == null || principal.getAccountId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        Account account = accountRepository.findById(principal.getAccountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));

        return toResponse(account);
    }

    @Transactional
    public AuthUserResponse updatePersonalData(AccountPrincipal principal, PersonalDataUpdateRequest request) {
        Customer customer = customerForPrincipal(principal);
        String firstName = requireText(request == null ? null : request.firstName(), "First name is required");
        String lastName = requireText(request == null ? null : request.lastName(), "Last name is required");

        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setPhoneNumber(normalizeOptional(request == null ? null : request.phoneNumber()));

        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public AuthUserResponse updateEmail(AccountPrincipal principal, EmailUpdateRequest request) {
        Customer customer = customerForPrincipal(principal);
        String newEmail = normalizeEmail(request == null ? null : request.newEmail());
        String repeatedNewEmail = normalizeEmail(request == null ? null : request.repeatedNewEmail());
        String password = normalizePassword(request == null ? null : request.password());

        if (!newEmail.equals(repeatedNewEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email addresses do not match");
        }

        if (!passwordEncoder.matches(password, customer.getPassword())) {
            throw invalidCredentials();
        }

        accountRepository.findByEmail(newEmail)
                .filter(existingAccount -> !existingAccount.getId().equals(customer.getId()))
                .ifPresent(existingAccount -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
                });

        customer.setEmail(newEmail);
        customer.setUsername(uniqueUsernameForEmailChange(newEmail, customer));

        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public AuthUserResponse updatePassword(AccountPrincipal principal, PasswordUpdateRequest request) {
        Customer customer = customerForPrincipal(principal);
        String oldPassword = normalizePassword(request == null ? null : request.oldPassword());
        String newPassword = normalizePassword(request == null ? null : request.newPassword());
        String repeatedNewPassword = normalizePassword(request == null ? null : request.repeatedNewPassword());

        if (!passwordEncoder.matches(oldPassword, customer.getPassword())) {
            throw invalidCredentials();
        }

        if (!newPassword.equals(repeatedNewPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        customer.setPassword(passwordEncoder.encode(newPassword));

        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public AuthUserResponse updateAddress(AccountPrincipal principal, AddressUpdateRequest request) {
        Customer customer = customerForPrincipal(principal);

        customer.setCountry(requireText(request == null ? null : request.country(), "Country is required"));
        customer.setCity(requireText(request == null ? null : request.city(), "City is required"));
        customer.setAddress(requireText(request == null ? null : request.address(), "Address is required"));
        customer.setZipCode(requireText(request == null ? null : request.zipCode(), "Zip code is required"));

        return toResponse(customerRepository.save(customer));
    }

    private Customer customerForPrincipal(AccountPrincipal principal) {
        if (principal == null || principal.getAccountId() == null || !principal.isCustomer()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        return customerRepository.findById(principal.getAccountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated"));
    }

    @Transactional(readOnly = true)
    public AuthUserResponse login(LoginRequest request) {
        String email = normalizeEmail(request == null ? null : request.email());
        String password = normalizePassword(request == null ? null : request.password());

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> invalidCredentials());

        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw invalidCredentials();
        }

        return toResponse(account);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (!normalizedEmail.contains("@") || normalizedEmail.startsWith("@") || normalizedEmail.endsWith("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid email is required");
        }

        return normalizedEmail;
    }

    private String normalizePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        return password;
    }

    private String uniqueUsernameFor(String email) {
        String base = email.substring(0, email.indexOf('@'))
                .replaceAll("[^a-zA-Z0-9._-]", "")
                .toLowerCase(Locale.ROOT);

        if (base.isBlank()) {
            base = "customer";
        }

        String candidate = base;
        int suffix = 1;

        while (accountRepository.existsByUsername(candidate)) {
            candidate = base + suffix;
            suffix++;
        }

        return candidate;
    }

    private String uniqueUsernameForEmailChange(String email, Customer customer) {
        String base = email.substring(0, email.indexOf('@'))
                .replaceAll("[^a-zA-Z0-9._-]", "")
                .toLowerCase(Locale.ROOT);

        if (base.isBlank()) {
            base = "customer";
        }

        String candidate = base;
        int suffix = 1;

        while (accountRepository.existsByUsername(candidate) && !candidate.equals(customer.getUsername())) {
            candidate = base + suffix;
            suffix++;
        }

        return candidate;
    }

    private String displayNameFor(String email) {
        String localPart = email.substring(0, email.indexOf('@')).trim();
        return localPart.isEmpty() ? "Customer" : localPart;
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    public AuthUserResponse toResponse(Account account) {
        String name = (account.getFirstName() + " " + account.getLastName()).trim();

        return new AuthUserResponse(
                account.getId(),
                account.getEmail(),
                account.getUsername(),
                name,
                formatAddress(account),
                account.getFirstName(),
                account.getLastName(),
                account.getAge(),
                account.getPhoneNumber(),
                account.getAddress(),
                account.getCity(),
                account.getCountry(),
                account.getZipCode(),
                roleFor(account)
        );
    }

    private String requireText(String value, String message) {
        String normalizedValue = normalizeOptional(value);

        if (normalizedValue == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        return normalizedValue;
    }

    private String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private String roleFor(Account account) {
        return new AccountPrincipal(account).getRole();
    }

    private String formatAddress(Account account) {
        return Stream.of(
                        account.getAddress(),
                        account.getCity(),
                        account.getCountry(),
                        account.getZipCode()
                )
                .filter(value -> value != null && !value.isBlank())
                .reduce((first, second) -> first + ", " + second)
                .orElse(null);
    }
}
