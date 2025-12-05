package com.library.management.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CsvHeaderMissingException.class)
    public ResponseEntity<Map<String, String>> HandlerCsvHeaderMissing(CsvHeaderMissingException ex) {
        Map<String, String> error =  new HashMap<>();
        error.put("error", "Encabezados faltantes en el archivo CSV");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Recurso no encontrado");
        error.put("message", ex.getMessage());
        error.put("resource", ex.getResourceName());
        error.put("field", ex.getFieldName());
        error.put("value", ex.getFieldValue().toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }


}
