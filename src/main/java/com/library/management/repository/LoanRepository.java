package com.library.management.repository;

import com.library.management.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("SELECT l FROM Loan l WHERE l.returnDate IS NULL")
    List<Loan> findActiveLoans();

    @Query("SELECT l.book, COUNT(l.book) AS borrowCount FROM Loan l GROUP BY l.book ORDER BY borrowCount DESC")
    List<Object[]> findMostLoanedBooks();
}
