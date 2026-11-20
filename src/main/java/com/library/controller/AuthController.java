package com.library.controller;

import com.library.constant.LibraryConstants;
import com.library.dto.ApiResponse;
import com.library.dto.LoginRequest;
import com.library.dto.StudentLoginRequest;
import com.library.dto.StudentRegisterRequest;
import com.library.entity.Member;
import com.library.exception.LibraryException;
import com.library.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Authentication Controller handling:
 * 1. Admin (Librarian) Login
 * 2. Student Login
 * 3. Student Registration / Account Creation
 * 4. Session Status Check & Logout
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public static final String SESSION_AUTH_KEY = "LIBRARIAN_LOGGED_IN";
    public static final String SESSION_ROLE_KEY = "USER_ROLE";         // "ADMIN" or "STUDENT"
    public static final String SESSION_USER_KEY = "USER_NAME";
    public static final String SESSION_EMAIL_KEY = "USER_EMAIL";
    public static final String SESSION_MEMBER_ID_KEY = "MEMBER_ID";

    @Value("${app.librarian.username:librarian}")
    private String expectedAdminUsername;

    @Value("${app.librarian.password:admin123}")
    private String expectedAdminPassword;

    private final MemberRepository memberRepository;

    public AuthController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // -------------------------------------------------------------
    // 1. Admin / Librarian Login
    // -------------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminLogin(
            @Valid @RequestBody LoginRequest request,
            HttpSession session) {

        if (expectedAdminUsername.equals(request.getUsername()) && expectedAdminPassword.equals(request.getPassword())) {
            session.setAttribute(SESSION_AUTH_KEY, true);
            session.setAttribute(SESSION_ROLE_KEY, "ADMIN");
            session.setAttribute(SESSION_USER_KEY, request.getUsername());

            Map<String, Object> data = new HashMap<>();
            data.put("authenticated", true);
            data.put("role", "ADMIN");
            data.put("username", request.getUsername());
            data.put("name", "Librarian Admin");

            return ResponseEntity.ok(ApiResponse.ok("Admin login successful", data));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid librarian credentials (Username or password incorrect)"));
    }

    // -------------------------------------------------------------
    // 2. Student Login
    // -------------------------------------------------------------
    @PostMapping("/student/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> studentLogin(
            @Valid @RequestBody StudentLoginRequest request,
            HttpSession session) {

        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        Optional<Member> memberOpt = memberRepository.findByEmailIgnoreCase(email);

        if (memberOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("No student account found with email: " + email + ". Please create an account."));
        }

        Member member = memberOpt.get();

        if (!member.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Your student account is inactive. Please contact the librarian."));
        }

        if (!member.getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Incorrect password. Please try again."));
        }

        // Setup session for authenticated student
        session.setAttribute(SESSION_ROLE_KEY, "STUDENT");
        session.setAttribute(SESSION_USER_KEY, member.getName());
        session.setAttribute(SESSION_EMAIL_KEY, member.getEmail());
        session.setAttribute(SESSION_MEMBER_ID_KEY, member.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("authenticated", true);
        data.put("role", "STUDENT");
        data.put("memberId", member.getId());
        data.put("name", member.getName());
        data.put("email", member.getEmail());

        return ResponseEntity.ok(ApiResponse.ok("Welcome back, " + member.getName() + "!", data));
    }

    // -------------------------------------------------------------
    // 3. New Student Registration / Account Creation
    // -------------------------------------------------------------
    @PostMapping("/student/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> studentRegister(
            @Valid @RequestBody StudentRegisterRequest request,
            HttpSession session) {

        String normalizedEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";

        if (memberRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new LibraryException("An account with email '" + normalizedEmail + "' already exists. Please login instead.");
        }

        Member newStudent = new Member(
                request.getName().trim(),
                normalizedEmail,
                request.getPhone().trim(),
                LocalDate.now(),
                LibraryConstants.STATUS_ACTIVE,
                request.getPassword()
        );

        Member saved = memberRepository.save(newStudent);

        // Automatically log in after registration
        session.setAttribute(SESSION_ROLE_KEY, "STUDENT");
        session.setAttribute(SESSION_USER_KEY, saved.getName());
        session.setAttribute(SESSION_EMAIL_KEY, saved.getEmail());
        session.setAttribute(SESSION_MEMBER_ID_KEY, saved.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("authenticated", true);
        data.put("role", "STUDENT");
        data.put("memberId", saved.getId());
        data.put("name", saved.getName());
        data.put("email", saved.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Student account created successfully! You are now logged in.", data));
    }

    // -------------------------------------------------------------
    // 4. Session Status Check
    // -------------------------------------------------------------
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus(
            HttpServletRequest request,
            HttpSession session) {
        String role = (String) session.getAttribute(SESSION_ROLE_KEY);

        // If session was lost (e.g. server restart) but client has persistent student identity, restore session
        if (role == null) {
            String headerId = request.getHeader("X-Student-Id");
            String headerEmail = request.getHeader("X-Student-Email");

            if (headerId != null && !headerId.trim().isEmpty()) {
                try {
                    Long id = Long.parseLong(headerId.trim());
                    Optional<Member> memberOpt = memberRepository.findById(id);
                    if (memberOpt.isPresent() && memberOpt.get().isActive()) {
                        Member member = memberOpt.get();
                        session.setAttribute(SESSION_ROLE_KEY, "STUDENT");
                        session.setAttribute(SESSION_USER_KEY, member.getName());
                        session.setAttribute(SESSION_EMAIL_KEY, member.getEmail());
                        session.setAttribute(SESSION_MEMBER_ID_KEY, member.getId());
                        role = "STUDENT";
                    }
                } catch (NumberFormatException ignored) {}
            } else if (headerEmail != null && !headerEmail.trim().isEmpty()) {
                Optional<Member> memberOpt = memberRepository.findByEmailIgnoreCase(headerEmail.trim());
                if (memberOpt.isPresent() && memberOpt.get().isActive()) {
                    Member member = memberOpt.get();
                    session.setAttribute(SESSION_ROLE_KEY, "STUDENT");
                    session.setAttribute(SESSION_USER_KEY, member.getName());
                    session.setAttribute(SESSION_EMAIL_KEY, member.getEmail());
                    session.setAttribute(SESSION_MEMBER_ID_KEY, member.getId());
                    role = "STUDENT";
                }
            }
        }

        boolean authenticated = role != null;

        Map<String, Object> data = new HashMap<>();
        data.put("authenticated", authenticated);
        data.put("role", role);

        if ("ADMIN".equals(role)) {
            data.put("username", session.getAttribute(SESSION_USER_KEY));
            data.put("name", "Librarian");
        } else if ("STUDENT".equals(role)) {
            data.put("memberId", session.getAttribute(SESSION_MEMBER_ID_KEY));
            data.put("name", session.getAttribute(SESSION_USER_KEY));
            data.put("email", session.getAttribute(SESSION_EMAIL_KEY));
        }

        return ResponseEntity.ok(ApiResponse.ok("Auth status", data));
    }

    // -------------------------------------------------------------
    // 5. Logout
    // -------------------------------------------------------------
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully"));
    }
}
