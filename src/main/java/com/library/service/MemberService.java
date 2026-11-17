package com.library.service;

import com.library.dto.MemberRequest;
import com.library.entity.Member;
import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> getAllMembers() {
        return memberRepository.findAllByOrderByCreatedAtDesc();
    }

    public Member getMemberById(String id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member with ID " + id + " not found"));
    }

    public Member addMember(MemberRequest req) {
        if (memberRepository.existsByEmail(req.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("Member with email '" + req.getEmail().trim() + "' already exists");
        }

        Member member = new Member();
        member.setName(req.getName().trim());
        member.setEmail(req.getEmail().trim().toLowerCase());
        member.setPhone(req.getPhone().trim());
        member.setMembershipDate(LocalDate.now());
        member.setStatus("ACTIVE");

        return memberRepository.save(member);
    }
}
