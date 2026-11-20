package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.entity.Wishlist;
import com.library.exception.UnauthorizedException;
import com.library.service.WishlistService;
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

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    private Long resolveMemberId(HttpSession session, Long fallbackMemberId) {
        Long sessionMemberId = (Long) session.getAttribute(AuthController.SESSION_MEMBER_ID_KEY);
        if (sessionMemberId != null) {
            return sessionMemberId;
        }
        if (fallbackMemberId != null) {
            return fallbackMemberId;
        }
        String role = (String) session.getAttribute(AuthController.SESSION_ROLE_KEY);
        if (!"STUDENT".equals(role)) {
            throw new UnauthorizedException("Student login required to access wishlist");
        }
        throw new UnauthorizedException("Student session not found. Please log in.");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Wishlist>>> getWishlist(
            @RequestParam(value = "memberId", required = false) Long queryMemberId,
            HttpSession session) {
        Long memberId = resolveMemberId(session, queryMemberId);
        List<Wishlist> wishlist = wishlistService.getMemberWishlist(memberId);
        return ResponseEntity.ok(ApiResponse.ok("Wishlist retrieved successfully", wishlist));
    }

    @GetMapping("/ids")
    public ResponseEntity<ApiResponse<List<Long>>> getWishlistBookIds(
            @RequestParam(value = "memberId", required = false) Long queryMemberId,
            HttpSession session) {
        Long memberId = resolveMemberId(session, queryMemberId);
        List<Long> ids = wishlistService.getWishlistBookIds(memberId);
        return ResponseEntity.ok(ApiResponse.ok("Wishlist book IDs retrieved", ids));
    }

    @PostMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Wishlist>> addToWishlist(
            @PathVariable Long bookId,
            @RequestParam(value = "memberId", required = false) Long queryMemberId,
            HttpSession session) {
        Long memberId = resolveMemberId(session, queryMemberId);
        Wishlist wishlist = wishlistService.addToWishlist(bookId, memberId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Book added to your wishlist ❤️", wishlist));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @PathVariable Long bookId,
            @RequestParam(value = "memberId", required = false) Long queryMemberId,
            HttpSession session) {
        Long memberId = resolveMemberId(session, queryMemberId);
        wishlistService.removeFromWishlist(bookId, memberId);
        return ResponseEntity.ok(ApiResponse.ok("Book removed from wishlist", null));
    }

    @PostMapping("/toggle/{bookId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleWishlist(
            @PathVariable Long bookId,
            @RequestParam(value = "memberId", required = false) Long queryMemberId,
            HttpSession session) {
        Long memberId = resolveMemberId(session, queryMemberId);
        boolean added = wishlistService.toggleWishlist(bookId, memberId);
        Map<String, Object> data = new HashMap<>();
        data.put("bookId", bookId);
        data.put("wishlisted", added);
        data.put("action", added ? "ADDED" : "REMOVED");

        String msg = added ? "Book added to your wishlist ❤️" : "Book removed from wishlist";
        return ResponseEntity.ok(ApiResponse.ok(msg, data));
    }
}
