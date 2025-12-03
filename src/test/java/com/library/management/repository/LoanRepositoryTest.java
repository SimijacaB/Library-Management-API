package com.library.management.repository;

import com.library.management.model.Author;
import com.library.management.model.Book;
import com.library.management.model.Loan;
import com.library.management.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LoanRepository loanRepository;

    @Test
    public void whenFindActiveLoans_thenReturnActiveLoans() {
        // given
        Author author = new Author();
        author.setName("Test Author");
        entityManager.persist(author);

        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor(author);
        book.setAvailable(false);
        entityManager.persist(book);

        User user = new User();
        user.setName("Test User");
        user.setRole("USER");
        entityManager.persist(user);

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setUser(user);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(15));
        entityManager.persist(loan);
        entityManager.flush();

        // when
        List<Loan> activeLoans = loanRepository.findActiveLoans();

        // then
        assertThat(activeLoans).isNotEmpty();
        assertThat(activeLoans.get(0).getBook().getTitle()).isEqualTo("Test Book");
    }
}
