package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.entity.Member;
import com.library.entity.Wishlist;
import com.library.exception.UnauthorizedException;
import com.library.repository.MemberRepository;
import com.library.service.WishlistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Student Wishlist functionality:
 * - View wishlisted books
 * - Get set of wishlisted book IDs
 * - Add book to wishlist
 * - Remove book from wishlist
 * - Toggle wishlist status
 */
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final MemberRepository memberRepository;

    public WishlistController(WishlistService wishlistService, MemberRepository memberRepository) {
        this.wishlistService = wishlistService;
        this.memberRepository = memberRepository;
    }

    private Long resolveMemberId(HttpSession session, HttpServletRequest request, Long fallbackMemberId) {
        Long sessionMemberId = (Long) session.getAttribute(AuthController.SESSION_MEMBER_ID_KEY);
        String role = (String) session.getAttribute(AuthController.SESSION_ROLE_KEY);
        if ("STUDENT".equals(role) && sessionMemberId != null) {
            return sessionMemberId;
        }

        // Restore from X-Student-Id header
        String headerId = request.getHeader("X-Student-Id");
        if (headerId != null && !headerId.trim().isEmpty()) {
            try {
                Long id = Long.parseLong(headerId.trim());
                Member member = memberRepository.findById(id).orElse(null);
                if (member != null && member.isActive()) {
                    session.setAttribute(AuthController.SESSION_ROLE_KEY, "STUDENT");
                    session.setAttribute(AuthController.SESSION_MEMBER_ID_KEY, member.getId());
                    session.setAttribute(AuthController.SESSION_USER_KEY, member.getName());
                    session.setAttribute(AuthController.SESSION_EMAIL_KEY, member.getEmail());
                    return member.getId();
                }
            } catch (NumberFormatException ignored) {}
        }

        // Restore from X-Student-Email header
        String headerEmail = request.getHeader("X-Student-Email");
        if (headerEmail != null && !headerEmail.trim().isEmpty()) {
            Member member = memberRepository.findByEmailIgnoreCase(headerEmail.trim()).orElse(null);
            if (member != null && member.isActive()) {
                session.setAttribute(AuthController.SESSION_ROLE_KEY, "STUDENT");
                session.setAttribute(AuthController.SESSION_MEMBER_ID_KEY, member.getId());
                session.setAttribute(AuthController.SESSION_USER_KEY, member.getName());
                session.setAttribute(AuthController.SESSION_EMAIL_KEY, member.getEmail());
                return member.getId();
            }
        }

        if (fallbackMemberId != null) {
            return fallbackMemberId;
        }

        throw new UnauthorizedException("Student login required to access wishlist");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Wishlist>>> getWishlist(
            @RequestParam(value = "memberId", required = false) Long queryMemberId,
            HttpServletRequest request,
            HttpSession session) {
        Long memberId = resolveMemberId(session, request, queryMemberId);
        List<Wishlist> wishlist = wishlistService.getMemberWishlist(memberId);
        return ResponseEntity.ok(ApiResponse.ok("Wishlist retrieved successfully", wishlist));
    }

    @GetMapping("/ids")
    public ResponseEntity<ApiResponse<List<Long>>> getWishlistBookIds(
            @RequestParam(value = "memberId", required = false) Long queryMemberId,
            HttpServletRequest request,
            HttpSession session) {
        Long memberId = resolveMemberId(session, request, queryMemberId);
        List<Long> ids = wishlistService.getWishlistBookIds(memberId);
        return ResponseEntity.ok(ApiResponse.ok("Wishlist book IDs retrieved", ids));
    }

    @PostMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Wishlist>> addToWishlist(
            @PathVariable Long bookId,
            @RequestParam(value = "memberId", required = false) Long queryMemberId,
            HttpServletRequest request,
            HttpSession session) {
        Long memberId = resolveMemberId(session, request, queryMemberId);
        Wishlist wishlist = wishlistService.addToWishlist(bookId, memberId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Book added to your wishlist ❤️", wishlist));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @PathVariable Long bookId,
            @RequestParam(value = "memberId", required = false) Long queryMemberId,
            HttpServletRequest request,
            HttpSession session) {
        Long memberId = resolveMemberId(session, request, queryMemberId);
        wishlistService.removeFromWishlist(bookId, memberId);
        return ResponseEntity.ok(ApiResponse.ok("Book removed from wishlist", null));
    }

    @PostMapping("/toggle/{bookId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleWishlist(
            @PathVariable Long bookId,
            @RequestParam(value = "memberId", required = false) Long queryMemberId,
            HttpServletRequest request,
            HttpSession session) {
        Long memberId = resolveMemberId(session, request, queryMemberId);
        boolean added = wishlistService.toggleWishlist(bookId, memberId);
        Map<String, Object> data = new HashMap<>();
        data.put("bookId", bookId);
        data.put("wishlisted", added);
        data.put("action", added ? "ADDED" : "REMOVED");

        String msg = added ? "Book added to your wishlist ❤️" : "Book removed from wishlist";
        return ResponseEntity.ok(ApiResponse.ok(msg, data));
    }
}
