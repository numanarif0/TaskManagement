package com.yzmglstm.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Veritabanı Benzersizlik Hataları (Mail/Telefon Çakışması)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleSqlConflict(DataIntegrityViolationException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "E-posta veya telefon numarası zaten sistemde kayıtlı.");
    }

    // 2. Özel Runtime Hataları (Güvenlik ve Bulunamadı Hataları)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST; // Varsayılan hata kodu

        // PDF 8.2: Yetkisiz işlemleri engelleme kuralı için 
        if (message != null && message.toLowerCase().contains("yetkisiz")) {
            status = HttpStatus.FORBIDDEN; // 403 Forbidden
        } 
        // Kayıt bulunamadığında
        else if (message != null && message.toLowerCase().contains("bulunamadı")) {
            status = HttpStatus.NOT_FOUND; // 404 Not Found
        }
        // Kayıtlı uyarısı için (Conflict)
        else if (message != null && message.toLowerCase().contains("kayıtlı")) {
            status = HttpStatus.CONFLICT; // 409 Conflict
        }

        return buildErrorResponse(status, message);
    }

    // Ortak Hata Taslağı Oluşturucu
    private ResponseEntity<Object> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return new ResponseEntity<>(body, status);
    }
}