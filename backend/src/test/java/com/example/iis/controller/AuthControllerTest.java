package com.example.iis.controller;

import com.example.iis.dto.AuthUserResponse;
import com.example.iis.dto.LoginRequest;
import com.example.iis.model.Customer;
import com.example.iis.security.CustomerPrincipal;
import com.example.iis.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.context.SecurityContextRepository;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {
    @Test
    void loginAuthenticatesAndSavesSecurityContext() {
        AuthService authService = mock(AuthService.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        SecurityContextRepository securityContextRepository = mock(SecurityContextRepository.class);
        AuthController controller = new AuthController(authService, authenticationManager, securityContextRepository);
        LoginRequest loginRequest = new LoginRequest("user@example.com", "secret");
        CustomerPrincipal principal = new CustomerPrincipal(
                new Customer("user", "secret", "User", "Customer", "user@example.com")
        );
        AuthUserResponse response = new AuthUserResponse(
                1L,
                "user@example.com",
                "user",
                "User",
                null,
                "User",
                "Customer",
                null,
                null,
                null,
                null,
                null,
                "CUSTOMER"
        );
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        when(authService.currentUser(principal)).thenReturn(response);

        assertSame(response, controller.login(loginRequest, servletRequest, servletResponse));
        verify(securityContextRepository).saveContext(any(), eq(servletRequest), eq(servletResponse));
    }

    @Test
    void meReturnsCurrentSessionUser() {
        AuthService authService = mock(AuthService.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        SecurityContextRepository securityContextRepository = mock(SecurityContextRepository.class);
        AuthController controller = new AuthController(authService, authenticationManager, securityContextRepository);
        CustomerPrincipal principal = new CustomerPrincipal(
                new Customer("user", "secret", "User", "Customer", "user@example.com")
        );
        AuthUserResponse response = new AuthUserResponse(
                1L,
                "user@example.com",
                "user",
                "User",
                null,
                "User",
                "Customer",
                null,
                null,
                null,
                null,
                null,
                "CUSTOMER"
        );

        when(authService.currentUser(principal)).thenReturn(response);

        assertSame(response, controller.me(principal));
    }
}
