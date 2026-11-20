package com.library.repository;

import com.library.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findAllByOrderByIssueDateDesc();

    List<Loan> findByStatusOrderByIssueDateDesc(String status);

    List<Loan> findByMemberIdOrderByIssueDateDesc(Long memberId);

    long countByStatus(String status);

    long countByMemberIdAndStatus(Long memberId, String status);
}
