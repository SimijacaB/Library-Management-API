package com.library.management.controller;

import com.library.management.dto.BookDTO;
import com.library.management.dto.BookRequestDTO;
import com.library.management.dto.BookResponseDTO;
import com.library.management.model.Book;
import com.library.management.service.BookService;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<Book> getAllBooks() {
        return bookService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Book book = bookService.findBookById(id);
        return ResponseEntity.ok(book);
    }

    @PostMapping
    public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody BookRequestDTO book) {

        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.save(book));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDTO> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequestDTO bookDetails) {

        return ResponseEntity.ok(bookService.update(id, bookDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<Book> searchBooks(@RequestParam String query) {
        return bookService.searchBooks(query);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadBooks(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please upload a CSV file.");
        }
        try {
            List<BookDTO> savedBooks = bookService.saveBooksFromCsv(file);
            return ResponseEntity.status(HttpStatus.OK).body(savedBooks);
        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error de validación: " + e.getMessage());
        } catch (IOException | CsvValidationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload books: " + e.getMessage());
        }
    }
}
