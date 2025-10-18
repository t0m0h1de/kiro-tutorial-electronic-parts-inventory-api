package com.example.inventory.exception;

/**
 * 部品コードが重複している場合にスローされる例外
 * 要件: 1.2, 2.2, 3.2, 5.5
 */
public class DuplicatePartCodeException extends RuntimeException {
    
    public DuplicatePartCodeException(String message) {
        super(message);
    }
    
    public DuplicatePartCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}