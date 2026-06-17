package com.example.iis.security;

import com.example.iis.model.Account;
import com.example.iis.model.Admin;
import com.example.iis.model.Botanist;
import com.example.iis.model.Customer;
import com.example.iis.model.Worker;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AccountPrincipal implements UserDetails {
    private final Long accountId;
    private final String accountUsername;
    private final String email;
    private final String password;
    private final String role;

    public AccountPrincipal(Account account) {
        this.accountId = account.getId();
        this.accountUsername = account.getUsername();
        this.email = account.getEmail();
        this.password = account.getPassword();
        this.role = roleFor(account);
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getAccountUsername() {
        return accountUsername;
    }

    public String getRole() {
        return role;
    }

    public boolean isCustomer() {
        return "CUSTOMER".equals(role);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    private String roleFor(Account account) {
        if (account instanceof Admin) {
            return "ADMIN";
        }

        if (account instanceof Botanist) {
            return "BOTANIST";
        }

        if (account instanceof Worker) {
            return "WORKER";
        }

        if (account instanceof Customer) {
            return "CUSTOMER";
        }

        return "CUSTOMER";
    }
}
