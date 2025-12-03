package com.library.management.controller;

import com.library.management.model.Author;
import com.library.management.model.Book;
import com.library.management.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
public class BookControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookService bookService;

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    public void givenBooks_whenGetBooks_thenReturnJsonArray() throws Exception {
        Author author = new Author();
        author.setName("Test Author");

        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor(author);

        given(bookService.findAll()).willReturn(Collections.singletonList(book));

        mvc.perform(get("/api/books")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Book"));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    public void whenGetBookById_thenReturnBook() throws Exception {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");

        given(bookService.findById(1L)).willReturn(Optional.of(book));

        mvc.perform(get("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void whenCreateBook_thenReturns200() throws Exception {
        mvc.perform(post("/api/books")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"New Book\",\"genre\":\"Fiction\",\"available\":true}"))
                .andExpect(status().isOk());
    }
}
