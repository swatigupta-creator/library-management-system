package com.library.service;

import com.library.dto.DashboardStatsResponse;
import com.library.dto.LoanIssueRequest;
import com.library.entity.Loan;

import java.util.List;

public interface LoanService {

    Loan issueBook(LoanIssueRequest req);

    Loan issueBook(Long bookId, Long memberId);

    Loan returnBook(Long loanId);

    List<Loan> getAllLoans();

    List<Loan> getAllLoans(String status);

    List<Loan> getLoansByStatus(String status);

    List<Loan> getLoansByMemberId(Long memberId);

    DashboardStatsResponse getDashboardStats();
}
