package com.library.management.service;

import com.library.management.dto.BookDTO;
import com.library.management.dto.BookRequestDTO;
import com.library.management.dto.BookResponseDTO;
import com.library.management.exception.CsvHeaderMissingException;
import com.library.management.exception.ResourceAlreadyExistsException;
import com.library.management.exception.ResourceNotFoundException;
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

    public Book findBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", "id", id));
    }

    public BookResponseDTO save(BookRequestDTO request) {
        // Buscar o crear autor automáticamente
        Author author = authorRepository.findByName(request.authorName())
                .orElseGet(() -> {
                    logger.info("Creando nuevo autor: {}", request.authorName());
                    Author newAuthor = new Author();
                    newAuthor.setName(request.authorName());
                    return authorRepository.save(newAuthor);
                });

        // Verificar si el libro ya existe
        if (bookRepository.existsByTitleAndAuthor_Name(request.title(), author.getName())) {
            throw new ResourceAlreadyExistsException("Libro", "título y autor",
                    request.title() + " por " + author.getName());
        }

        Book book = Book.builder()
                .title(request.title())
                .author(author)
                .genre(request.genre())
                .available(true)
                .build();

        Book savedBook = bookRepository.save(book);

        return new BookResponseDTO(
                savedBook.getTitle(),
                savedBook.getAuthor().getName(),
                savedBook.getGenre(),
                savedBook.isAvailable()
        );
    }


    public BookResponseDTO update(Long id, BookRequestDTO request) {

        // 1. Buscar libro existente
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", "id", id));

        // 2. Buscar o crear autor
        Author author = authorRepository.findByName(request.authorName())
                .orElseGet(() -> {
                    Author newAuthor = new Author();
                    newAuthor.setName(request.authorName());
                    return authorRepository.save(newAuthor);
                });

        // 3. Validar si otro libro (no este) tiene este título+autor
        boolean exists = bookRepository.existsByTitleAndAuthor_Name(
                request.title(),
                author.getName()
        );

        if (exists) {
            throw new ResourceAlreadyExistsException(
                    "Libro", "título y autor",
                    request.title() + " / " + author.getName()
            );
        }

        // 4. Actualizar campos
        existingBook.setTitle(request.title());
        existingBook.setAuthor(author);
        existingBook.setGenre(request.genre());
        // existingBook.setAvailable(request.available()); // si lo manejas

        // 5. Guardar
        Book saved = bookRepository.save(existingBook);

        // 6. Convertir a DTO de respuesta
        return new BookResponseDTO(
                saved.getTitle(),
                saved.getAuthor().getName(),
                saved.getGenre(),
                saved.isAvailable()
        );
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
            String[] header = reader.readNext();
            List<Book> books = new ArrayList<>();

            // VALIDACIÓN ESTRICTA: El CSV DEBE contener encabezados válidos
            if (header == null || header.length == 0) {
                logger.error("El archivo CSV está vacío o no contiene encabezados");
                throw new CsvHeaderMissingException("El archivo CSV debe contener encabezados en la primera fila");
            }

            logger.info("Primera fila leída con {} columnas: {}", header.length, String.join(", ", header));

            // Verificar que la primera fila contiene encabezados válidos
            boolean hasValidHeader = false;
            for (String col : header) {
                if (col != null) {
                    String colLower = col.trim().toLowerCase();
                    if (colLower.equals("title") || colLower.equals("titulo") ||
                        colLower.equals("book") || colLower.equals("libro") ||
                        colLower.equals("author") || colLower.equals("autor") ||
                        colLower.equals("genre") || colLower.equals("genero") ||
                        colLower.equals("name") || colLower.equals("nombre")) {
                        hasValidHeader = true;
                        break;
                    }
                }
            }

            if (!hasValidHeader) {
                logger.error("El archivo CSV no contiene encabezados válidos. Primera fila: {}", String.join(", ", header));
                throw new CsvHeaderMissingException(
                    "El archivo CSV debe contener encabezados válidos en la primera fila. " +
                    "Encabezados esperados: 'titulo/title', 'autor/author', 'genero/genre'. " +
                    "Se encontró: " + String.join(", ", header)
                );
            }

            logger.info("Encabezados válidos detectados, procesando datos a partir de la fila 2");
            int row = 2; // Comenzar desde la segunda fila (primera fila = encabezado)

            while ((nextLine = reader.readNext()) != null) {
                // Skip completely empty lines to avoid infinite loops
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

        // 1) Validar si el libro ya existe en la base de datos
        if (bookRepository.existsByTitleAndAuthor_Name(title, authorName)) {
            logger.warn("Fila {}: el libro '{}' por '{}' ya existe en la base de datos, omitiendo", rowNumber, title, authorName);
            return;
        }

        // 2) Validar duplicados en el lote actual del CSV
        boolean duplicateInBatch = books.stream().anyMatch(b ->
                title.equalsIgnoreCase(safeTrim(b.getTitle())) &&
                authorName.equalsIgnoreCase(safeTrim(b.getAuthor().getName()))
        );
        if (duplicateInBatch) {
            logger.warn("Fila {}: libro '{}' por '{}' ya está presente en la carga actual, omitiendo", rowNumber, title, authorName);
            return;
        }

        // Buscar o crear autor
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