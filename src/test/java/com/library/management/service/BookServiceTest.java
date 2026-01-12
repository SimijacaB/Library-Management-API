package com.library.management.service;

import com.library.management.dto.BookRequestDTO;
import com.library.management.dto.BookResponseDTO;
import com.library.management.model.Author;
import com.library.management.model.Book;
import com.library.management.repository.AuthorRepository;
import com.library.management.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private BookService bookService;

    private Book book;

    @BeforeEach
    public void setUp() {
        Author author = new Author();
        author.setId(1L);
        author.setName("Test Author");

        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setAuthor(author);
    }

    @Test
    public void whenFindAll_thenReturnBookList() {
        given(bookRepository.findAll()).willReturn(Collections.singletonList(book));

        List<Book> result = bookService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getTitle());
        verify(bookRepository).findAll();
    }

    @Test
    public void whenFindById_thenReturnBook() {
        given(bookRepository.findById(1L)).willReturn(Optional.of(book));

        Book result = bookService.findBookById(1L);

        assertNotNull(result);
        assertEquals("Test Book", result.getTitle());
        verify(bookRepository).findById(1L);
    }

    @Test
    public void whenSaveBook_thenReturnSavedBook() {
        // Arrange
        BookRequestDTO request = new BookRequestDTO(
                "Test Book",
                "Test Author",
                "Fiction"
        );

        Author author = new Author();
        author.setId(1L);
        author.setName("Test Author");

        Book book = new Book(null, "Test Book", author, "Fiction", true);

        given(authorRepository.findByName("Test Author")).willReturn(Optional.of(author));
        given(bookRepository.existsByTitleAndAuthor_Name("Test Book", "Test Author")).willReturn(false);
        given(bookRepository.save(any(Book.class))).willReturn(book);

        // Act
        BookResponseDTO result = bookService.save(request);

        // Assert
        assertNotNull(result);
        assertEquals("Test Book", result.title());
        assertEquals("Test Author", result.author());
        assertEquals("Fiction", result.genre());
        assertTrue(result.available());
    }
}
