package com.example.iis.security;

import com.example.iis.repository.CustomerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CustomerUserDetailsService implements UserDetailsService {
    private final CustomerRepository customerRepository;

    public CustomerUserDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.trim().isEmpty()) {
            throw new UsernameNotFoundException("Customer not found");
        }

        String email = username.trim().toLowerCase(Locale.ROOT);
        return customerRepository.findByEmail(email)
                .map(CustomerPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found"));
    }
}
