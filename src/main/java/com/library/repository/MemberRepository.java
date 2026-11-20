package com.library.repository;

import com.library.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByEmailIgnoreCase(String email);

    List<Member> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
}
