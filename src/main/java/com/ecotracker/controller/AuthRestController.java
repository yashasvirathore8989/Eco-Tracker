package com.ecotracker.controller;

import com.ecotracker.dto.ApiResponse;
import com.ecotracker.dto.LoginRequest;
import com.ecotracker.dto.RegistrationRequest;
import com.ecotracker.dto.UserResponse;
import com.ecotracker.entity.User;
import com.ecotracker.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthRestController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    public AuthRestController(AuthService authService, AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegistrationRequest request) {
        User registered = authService.registerUser(request);
        UserResponse responseDto = authService.getUserProfile(registered.getEmail());

        ApiResponse<UserResponse> response = new ApiResponse<>(
                registered.getId(),
                "REGISTERED",
                "User registered successfully. You may now sign in."
        );
        response.setData(responseDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserResponse userResponse = authService.getUserProfile(request.getEmail().trim().toLowerCase());
        ApiResponse<UserResponse> response = new ApiResponse<>(
                userResponse.getId(),
                "AUTHENTICATED",
                "Login successful"
        );
        response.setData(userResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.ok(new ApiResponse<>("Logged out successfully"));
    }
}
