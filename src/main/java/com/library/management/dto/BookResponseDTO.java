package com.library.management.dto;

import com.library.management.model.Author;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookResponseDTO(
        String title,
        String author,
        String genre,
        Boolean available
) {

}
