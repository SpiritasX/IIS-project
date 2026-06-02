package com.example.iis.controller;

import com.example.iis.dto.AddressUpdateRequest;
import com.example.iis.dto.AuthUserResponse;
import com.example.iis.dto.EmailUpdateRequest;
import com.example.iis.dto.LoginRequest;
import com.example.iis.dto.PasswordUpdateRequest;
import com.example.iis.dto.PersonalDataUpdateRequest;
import com.example.iis.dto.SignupRequest;
import com.example.iis.security.CustomerPrincipal;
import com.example.iis.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(
            AuthService authService,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository
    ) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken) {
        return Map.of(
                "headerName", csrfToken.getHeaderName(),
                "parameterName", csrfToken.getParameterName(),
                "token", csrfToken.getToken()
        );
    }

    @GetMapping("/me")
    public AuthUserResponse me(@AuthenticationPrincipal CustomerPrincipal principal) {
        return authService.currentUser(principal);
    }

    @PatchMapping("/profile/personal")
    public AuthUserResponse updatePersonalData(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @RequestBody PersonalDataUpdateRequest request
    ) {
        return authService.updatePersonalData(principal, request);
    }

    @PatchMapping("/profile/email")
    public AuthUserResponse updateEmail(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @RequestBody EmailUpdateRequest request
    ) {
        return authService.updateEmail(principal, request);
    }

    @PatchMapping("/profile/password")
    public AuthUserResponse updatePassword(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @RequestBody PasswordUpdateRequest request
    ) {
        return authService.updatePassword(principal, request);
    }

    @PatchMapping("/profile/address")
    public AuthUserResponse updateAddress(
            @AuthenticationPrincipal CustomerPrincipal principal,
            @RequestBody AddressUpdateRequest request
    ) {
        return authService.updateAddress(principal, request);
    }

    @PostMapping("/signup")
    public AuthUserResponse signup(
            @RequestBody SignupRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        authService.signup(request);
        return authenticate(request.email(), request.password(), servletRequest, servletResponse);
    }

    @PostMapping("/login")
    public AuthUserResponse login(
            @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        return authenticate(request.email(), request.password(), servletRequest, servletResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        securityContextRepository.saveContext(SecurityContextHolder.createEmptyContext(), request, response);

        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        expireCookie(response, "JSESSIONID");
        expireCookie(response, "XSRF-TOKEN");

        return ResponseEntity.noContent().build();
    }

    private AuthUserResponse authenticate(
            String email,
            String password,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            request.getSession();
            request.changeSessionId();

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            securityContextRepository.saveContext(securityContext, request, response);

            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomerPrincipal customerPrincipal) {
                return authService.currentUser(customerPrincipal);
            }

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
    }

    private void expireCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
