package com.library.management.service;

import com.library.management.dto.BookDTO;
import com.library.management.model.Author;
import com.library.management.model.Book;
import com.library.management.repository.AuthorRepository;
import com.library.management.repository.BookRepository;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    public Book save(Book book) {
        return bookRepository.save(book);
    }

    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    public List<Book> searchBooks(String query) {
        return bookRepository.searchBooks(query);
    }

    @Transactional
    public List<BookDTO> saveBooksFromCsv(MultipartFile file) throws IOException, CsvValidationException {
        logger.info("Iniciando carga de libros desde CSV: {}", file.getOriginalFilename());

        CSVParser parser = new CSVParserBuilder()
                .withSeparator(';')
                .build();

        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                .withCSVParser(parser)
                .build()) {
            String[] nextLine;
            // Attempt to read header; if absent, proceed as data.
            String[] header = reader.readNext();
            List<Book> books = new ArrayList<>();
            int row = 1; // if header present, first data row will be 2; adjust below.
            boolean headerLooksLikeHeader = false;

            if (header != null) {
                logger.info("Primera fila leída con {} columnas: {}", header.length, String.join(", ", header));
                row = 2;
                // Heuristic: if the first line contains typical header names, skip as header.
                String h0 = header.length > 0 ? header[0].trim().toLowerCase() : "";
                headerLooksLikeHeader = h0.contains("title") || h0.contains("titulo") || h0.contains("book");

                if (!headerLooksLikeHeader) {
                    logger.info("Primera fila parece ser datos, procesando como libro");
                    processRow(header, 1, books);
                    row = 2;
                } else {
                    logger.info("Primera fila detectada como encabezado, omitiendo");
                }
            }

            while ((nextLine = reader.readNext()) != null) {
                // Skip completely empty lines to avoid infinite loops
                // Check if the array is empty or all values are blank
                if (nextLine.length == 0) {
                    logger.debug("Fila {} está vacía (length=0), omitiendo", row);
                    row++;
                    continue;
                }

                boolean allEmpty = true;
                for (String value : nextLine) {
                    if (value != null && !value.trim().isEmpty()) {
                        allEmpty = false;
                        break;
                    }
                }

                if (allEmpty) {
                    logger.debug("Fila {} tiene todos los valores vacíos, omitiendo", row);
                } else {
                    processRow(nextLine, row, books);
                }
                row++;
            }

            logger.info("Total de libros procesados del CSV: {}", books.size());

            List<Book> savedBooks = new ArrayList<>();
            if (!books.isEmpty()) {
                savedBooks = bookRepository.saveAll(books);
                logger.info("Libros guardados en la base de datos: {}", savedBooks.size());
            } else {
                logger.warn("No se encontraron libros válidos para guardar");
            }

            // Convertir a DTO y retornar
            List<BookDTO> bookDTOs = savedBooks.stream()
                    .map(book -> new BookDTO(
                            book.getId(),
                            book.getTitle(),
                            book.getAuthor().getId(),
                            book.getAuthor().getName(),
                            book.getGenre(),
                            book.isAvailable()
                    ))
                    .collect(Collectors.toList());

            logger.info("Retornando {} libros como DTOs", bookDTOs.size());
            return bookDTOs;
        }
    }

    // Helper to validate and convert a CSV row into a Book, collecting into 'books' if valid.
    private void processRow(String[] rowValues, int rowNumber, List<Book> books) {
        if (rowValues == null) {
            logger.debug("Fila {} es null, omitiendo", rowNumber);
            return;
        }

        // Skip completely empty or whitespace-only rows
        boolean allBlank = true;
        for (String v : rowValues) {
            if (v != null && !v.trim().isEmpty()) {
                allBlank = false;
                break;
            }
        }
        if (allBlank) {
            logger.debug("Fila {} está completamente vacía, omitiendo", rowNumber);
            return;
        }

        // Expect at least 3 columns: title, author, genre
        if (rowValues.length < 3) {
            logger.warn("Fila {} tiene menos de 3 columnas ({}), omitiendo", rowNumber, rowValues.length);
            return;
        }

        String title = safeTrim(rowValues[0]);
        String authorName = safeTrim(rowValues[1]);
        String genre = safeTrim(rowValues[2]);

        logger.debug("Procesando fila {}: titulo='{}', autor='{}', genero='{}'", rowNumber, title, authorName, genre);

        // Basic validation
        if (title.isEmpty() || authorName.isEmpty()) {
            logger.warn("Fila {} tiene datos inválidos (título o autor vacío), omitiendo", rowNumber);
            return;
        }

        Author author = authorRepository.findByName(authorName)
                .orElseGet(() -> {
                    logger.info("Creando nuevo autor: {}", authorName);
                    Author newAuthor = new Author();
                    newAuthor.setName(authorName);
                    return authorRepository.save(newAuthor);
                });

        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genre);
        book.setAvailable(true);
        books.add(book);

        logger.info("Libro agregado a la lista para guardar: '{}' por {}", title, authorName);
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}