package com.library.service;

import com.library.dto.MemberRequest;
import com.library.entity.Member;

import java.util.List;

public interface MemberService {

    List<Member> getAllMembers();

    List<Member> getAllMembers(String search);

    Member getMemberById(Long id);

    Member addMember(MemberRequest req);
}
