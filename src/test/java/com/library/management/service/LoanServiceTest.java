package com.library.management.service;

import com.library.management.model.Book;
import com.library.management.model.Loan;
import com.library.management.model.User;
import com.library.management.repository.BookRepository;
import com.library.management.repository.LoanRepository;
import com.library.management.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoanService loanService;

    @Test
    public void whenBorrowBook_thenSuccess() {
        // given
        Book book = new Book();
        book.setId(1L);
        book.setAvailable(true);

        User user = new User();
        user.setId(1L);

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setUser(user);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(15));

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        // when
        Loan createdLoan = loanService.borrowBook(1L, 1L);

        // then
        assertThat(createdLoan).isNotNull();
        assertThat(createdLoan.getBook().isAvailable()).isFalse();
    }

    @Test
    public void whenBorrowBook_thenBookNotFound() {
        // given
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> {
            loanService.borrowBook(1L, 1L);
        });
    }

    @Test
    public void whenReturnBook_thenSuccess() {
        // given
        Book book = new Book();
        book.setId(1L);
        book.setAvailable(false);

        Loan loan = new Loan();
        loan.setId(1L);
        loan.setBook(book);
        loan.setDueDate(LocalDate.now().minusDays(1)); // Overdue

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // when
        Loan returnedLoan = loanService.returnBook(1L);

        // then
        assertThat(returnedLoan).isNotNull();
        assertThat(returnedLoan.getReturnDate()).isNotNull();
        assertThat(returnedLoan.getBook().isAvailable()).isTrue();
    }
}
