package com.example.inventory.exception;

/**
 * 在庫数量が不足している場合にスローされる例外
 * 要件: 5.5
 */
public class InsufficientStockException extends RuntimeException {
    
    public InsufficientStockException(String message) {
        super(message);
    }
    
    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}