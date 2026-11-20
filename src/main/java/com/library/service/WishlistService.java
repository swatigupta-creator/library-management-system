package com.library.service;

import com.library.entity.Wishlist;
import java.util.List;

public interface WishlistService {

    Wishlist addToWishlist(Long bookId, Long memberId);

    void removeFromWishlist(Long bookId, Long memberId);

    boolean toggleWishlist(Long bookId, Long memberId);

    List<Wishlist> getMemberWishlist(Long memberId);

    List<Long> getWishlistBookIds(Long memberId);

    boolean isWishlisted(Long bookId, Long memberId);
}
