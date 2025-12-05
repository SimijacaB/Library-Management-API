package com.library.management.dto;

import com.library.management.model.Author;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookRequestDTO(
        @NotBlank(message = "El título es obligatorio")
        String title,

        @NotBlank(message = "El nombre del autor es obligatorio")
        String authorName,

        @NotBlank(message = "El género es obligatorio")
        String genre

) {
}
