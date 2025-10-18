package com.example.inventory.exception;

/**
 * 部品が見つからない場合にスローされる例外
 * 要件: 1.2, 2.2, 3.2, 5.5
 */
public class PartNotFoundException extends RuntimeException {
    
    public PartNotFoundException(String message) {
        super(message);
    }
    
    public PartNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}