package com.yzmglstm.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public Map<String, Object> handleSqlConflict(Exception ex) {
    return Map.of("error", "conflict", "message", "E-posta veya telefon zaten kayıtlı.");
  }

  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public Map<String, Object> handleDup(Exception ex) {
    if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("kayıtlı")) {
      return Map.of("error", "conflict", "message", ex.getMessage());
    }
    return Map.of("error", "error", "message", ex.getMessage());
  }
}
