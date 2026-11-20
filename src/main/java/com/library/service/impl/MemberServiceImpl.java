package com.library.service.impl;

import com.library.constant.LibraryConstants;
import com.library.dto.MemberRequest;
import com.library.entity.Member;
import com.library.exception.LibraryException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.MemberRepository;
import com.library.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service Implementation demonstrating:
 * 1. Interfaces & Abstraction (Implements MemberService)
 * 2. Polymorphism & Method Overriding (@Override)
 * 3. Method Overloading (getAllMembers() vs getAllMembers(String search))
 * 4. Collections & Generics (List<Member>)
 * 5. Loops, Conditions & String handling (Email lowercase normalization, validation)
 * 6. Access Control & Final keywords
 */
@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // Overloaded Method 1: List all members
    @Override
    @Transactional(readOnly = true)
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    // Overloaded Method 2: List members filtered by search query
    @Override
    @Transactional(readOnly = true)
    public List<Member> getAllMembers(String search) {
        if (search == null || search.trim().isEmpty()) {
            return getAllMembers();
        }
        String query = search.trim();
        return memberRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }

    @Override
    @Transactional(readOnly = true)
    public Member getMemberById(Long id) {
        if (id == null) {
            throw new LibraryException("Member ID must not be null");
        }
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member with ID " + id + " was not found"));
    }

    @Override
    @Transactional
    public Member addMember(MemberRequest req) {
        // String handling & validation
        String normalizedEmail = req.getEmail() != null ? req.getEmail().trim().toLowerCase() : "";
        if (normalizedEmail.isEmpty()) {
            throw new LibraryException("Member email address cannot be empty");
        }

        if (memberRepository.existsByEmail(normalizedEmail)) {
            throw new LibraryException("A member with email '" + normalizedEmail + "' is already registered");
        }

        // Creating Member object using overloaded constructor
        Member member = new Member(
                req.getName().trim(),
                normalizedEmail,
                req.getPhone() != null ? req.getPhone().trim() : "",
                LocalDate.now(),
                LibraryConstants.STATUS_ACTIVE,
                "student123"
        );

        return memberRepository.save(member);
    }
}
