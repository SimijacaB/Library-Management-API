package com.library.management.repository;

import com.library.management.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByTitleContaining(String title);

    List<Book> findByAuthorNameContaining(String authorName);

    List<Book> findByGenre(String genre);

    @Query("SELECT b FROM Book b WHERE b.title LIKE %:query% OR b.author.name LIKE %:query% OR b.genre LIKE %:query%")
    List<Book> searchBooks(@Param("query") String query);
}
