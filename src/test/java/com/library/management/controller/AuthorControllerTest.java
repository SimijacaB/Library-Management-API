package com.library.management.controller;

import com.library.management.model.Author;
import com.library.management.service.AuthorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthorController.class)
public class AuthorControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthorService authorService;

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    public void givenAuthors_whenGetAuthors_thenReturnJsonArray() throws Exception {
        Author author = new Author();
        author.setName("Test Author");

        given(authorService.findAll()).willReturn(Collections.singletonList(author));

        mvc.perform(get("/api/authors")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Author"));
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    public void whenGetAuthorById_thenReturnAuthor() throws Exception {
        Author author = new Author();
        author.setId(1L);
        author.setName("Test Author");

        given(authorService.findById(1L)).willReturn(Optional.of(author));

        mvc.perform(get("/api/authors/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Author"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void whenCreateAuthor_thenReturns200() throws Exception {
        mvc.perform(post("/api/authors")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New Author\"}"))
                .andExpect(status().isOk());
    }
}
