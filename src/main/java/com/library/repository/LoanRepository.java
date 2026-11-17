package com.library.repository;

import com.library.entity.Loan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends MongoRepository<Loan, String> {

    List<Loan> findAllByOrderByCreatedAtDesc();

    List<Loan> findByStatusOrderByCreatedAtDesc(String status);

    List<Loan> findByBookId(String bookId);

    List<Loan> findByMemberId(String memberId);

    boolean existsByBookIdAndStatus(String bookId, String status);

    long countByStatus(String status);
}
