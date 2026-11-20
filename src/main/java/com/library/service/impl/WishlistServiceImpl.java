package com.library.service.impl;

import com.library.entity.Book;
import com.library.entity.Member;
import com.library.entity.Wishlist;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;
import com.library.repository.WishlistRepository;
import com.library.service.WishlistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public WishlistServiceImpl(WishlistRepository wishlistRepository,
                               BookRepository bookRepository,
                               MemberRepository memberRepository) {
        this.wishlistRepository = wishlistRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public Wishlist addToWishlist(Long bookId, Long memberId) {
        return wishlistRepository.findByMemberIdAndBookId(memberId, bookId)
                .orElseGet(() -> {
                    Book book = bookRepository.findById(bookId)
                            .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
                    Member member = memberRepository.findById(memberId)
                            .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));
                    Wishlist wishlist = new Wishlist(member, book);
                    return wishlistRepository.save(wishlist);
                });
    }

    @Override
    public void removeFromWishlist(Long bookId, Long memberId) {
        wishlistRepository.deleteByMemberIdAndBookId(memberId, bookId);
    }

    @Override
    public boolean toggleWishlist(Long bookId, Long memberId) {
        if (wishlistRepository.existsByMemberIdAndBookId(memberId, bookId)) {
            wishlistRepository.deleteByMemberIdAndBookId(memberId, bookId);
            return false; // Removed from wishlist
        } else {
            addToWishlist(bookId, memberId);
            return true; // Added to wishlist
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Wishlist> getMemberWishlist(Long memberId) {
        return wishlistRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getWishlistBookIds(Long memberId) {
        return wishlistRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(w -> w.getBook().getId())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWishlisted(Long bookId, Long memberId) {
        return wishlistRepository.existsByMemberIdAndBookId(memberId, bookId);
    }
}
