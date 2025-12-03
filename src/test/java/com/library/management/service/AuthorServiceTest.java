package com.library.management.service;

import com.library.management.model.Author;
import com.library.management.repository.AuthorRepository;
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
public class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorService authorService;

    @Test
    public void whenFindAll_thenReturnAuthorList() {
        // given
        Author author = new Author();
        author.setName("Test Author");

        List<Author> authorList = Collections.singletonList(author);
        when(authorRepository.findAll()).thenReturn(authorList);

        // when
        List<Author> foundAuthors = authorService.findAll();

        // then
        assertThat(foundAuthors).isNotEmpty();
        assertThat(foundAuthors.get(0).getName()).isEqualTo("Test Author");
    }

    @Test
    public void whenFindById_thenReturnAuthor() {
        // given
        Author author = new Author();
        author.setId(1L);
        author.setName("Test Author");

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        // when
        Optional<Author> foundAuthor = authorService.findById(1L);

        // then
        assertThat(foundAuthor).isPresent();
        assertThat(foundAuthor.get().getName()).isEqualTo("Test Author");
    }
}
