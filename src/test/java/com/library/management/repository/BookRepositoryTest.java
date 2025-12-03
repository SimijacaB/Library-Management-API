package com.library.management.repository;

import com.library.management.model.Author;
import com.library.management.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    @Test
    public void whenFindByTitle_thenReturnBook() {
        // given
        Author author = new Author();
        author.setName("Test Author");
        entityManager.persist(author);

        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor(author);
        book.setGenre("Fiction");
        book.setAvailable(true);
        entityManager.persist(book);
        entityManager.flush();

        // when
        List<Book> found = bookRepository.findByTitleContaining("Test Book");

        // then
        assertThat(found).isNotEmpty();
        assertThat(found.get(0).getTitle()).isEqualTo(book.getTitle());
    }
}
