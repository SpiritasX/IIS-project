package com.example.iis.security;

import com.example.iis.model.Customer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomerPrincipal implements UserDetails {
    private final Long customerId;
    private final String accountUsername;
    private final String email;
    private final String password;

    public CustomerPrincipal(Customer customer) {
        this.customerId = customer.getId();
        this.accountUsername = customer.getUsername();
        this.email = customer.getEmail();
        this.password = customer.getPassword();
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getAccountUsername() {
        return accountUsername;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
