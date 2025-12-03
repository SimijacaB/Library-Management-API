package com.library.management.dto;

public record BookDTO(
        Long id,
        String title,
        Long authorId,
        String authorName,
        String genre,
        boolean available
) {}
