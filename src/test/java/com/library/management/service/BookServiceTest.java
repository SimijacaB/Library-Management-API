package com.library.management.service;

import com.library.management.model.Author;
import com.library.management.model.Book;
import com.library.management.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    public void whenFindAll_thenReturnBookList() {
        // given
        Author author = new Author();
        author.setName("Test Author");

        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor(author);
        book.setGenre("Fiction");
        book.setAvailable(true);

        List<Book> bookList = Collections.singletonList(book);
        when(bookRepository.findAll()).thenReturn(bookList);

        // when
        List<Book> foundBooks = bookService.findAll();

        // then
        assertThat(foundBooks).isNotEmpty();
        assertThat(foundBooks.get(0).getTitle()).isEqualTo("Test Book");
    }

    @Test
    public void whenFindById_thenReturnBook() {
        // given
        Author author = new Author();
        author.setName("Test Author");

        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setAuthor(author);
        book.setGenre("Fiction");
        book.setAvailable(true);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        // when
        Optional<Book> foundBook = bookService.findById(1L);

        // then
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getTitle()).isEqualTo("Test Book");
    }
}
