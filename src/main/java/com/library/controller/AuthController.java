package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.LoginRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String SESSION_AUTH_KEY = "LIBRARIAN_LOGGED_IN";
    public static final String SESSION_USER_KEY = "LIBRARIAN_USERNAME";

    @Value("${app.librarian.username:librarian}")
    private String expectedUsername;

    @Value("${app.librarian.password:admin123}")
    private String expectedPassword;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @Valid @RequestBody LoginRequest request,
            HttpSession session) {

        if (expectedUsername.equals(request.getUsername()) && expectedPassword.equals(request.getPassword())) {
            session.setAttribute(SESSION_AUTH_KEY, true);
            session.setAttribute(SESSION_USER_KEY, request.getUsername());

            Map<String, Object> data = new HashMap<>();
            data.put("authenticated", true);
            data.put("username", request.getUsername());

            return ResponseEntity.ok(ApiResponse.ok("Login successful", data));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully"));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus(HttpSession session) {
        Object isAuth = session.getAttribute(SESSION_AUTH_KEY);
        boolean authenticated = Boolean.TRUE.equals(isAuth);

        Map<String, Object> data = new HashMap<>();
        data.put("authenticated", authenticated);
        data.put("username", authenticated ? session.getAttribute(SESSION_USER_KEY) : null);

        return ResponseEntity.ok(ApiResponse.ok("Auth status", data));
    }
}
