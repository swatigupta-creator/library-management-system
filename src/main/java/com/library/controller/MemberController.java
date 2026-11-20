package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.MemberRequest;
import com.library.entity.Member;
import com.library.exception.UnauthorizedException;
import com.library.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    private void checkLibrarianAuth(HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute(AuthController.SESSION_AUTH_KEY))) {
            throw new UnauthorizedException("Unauthorized: Librarian login required");
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Member>>> getAllMembers(HttpSession session) {
        checkLibrarianAuth(session);
        List<Member> members = memberService.getAllMembers();
        return ResponseEntity.ok(ApiResponse.ok("Members fetched successfully", members));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Member>> getMemberById(@PathVariable Long id, HttpSession session) {
        checkLibrarianAuth(session);
        Member member = memberService.getMemberById(id);
        return ResponseEntity.ok(ApiResponse.ok("Member details fetched", member));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Member>> addMember(
            @Valid @RequestBody MemberRequest request,
            HttpSession session) {
        checkLibrarianAuth(session);
        Member created = memberService.addMember(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Member registered successfully", created));
    }
}
