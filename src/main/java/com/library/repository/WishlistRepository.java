package com.library.repository;

import com.library.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    Optional<Wishlist> findByMemberIdAndBookId(Long memberId, Long bookId);

    boolean existsByMemberIdAndBookId(Long memberId, Long bookId);

    void deleteByMemberIdAndBookId(Long memberId, Long bookId);

    long countByMemberId(Long memberId);
}
