package com.example.inventory.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * カスタム例外クラスの単体テスト
 * 要件: 1.2, 2.2, 3.2, 5.5
 */
class CustomExceptionsTest {

    /**
     * PartNotFoundExceptionのテスト
     * 要件: 1.2, 2.2, 3.2, 5.5
     */
    @Test
    void partNotFoundException_WithMessage_ShouldCreateCorrectException() {
        // Given
        String message = "部品コード 'TEST001' が見つかりません";

        // When
        PartNotFoundException exception = new PartNotFoundException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void partNotFoundException_WithMessageAndCause_ShouldCreateCorrectException() {
        // Given
        String message = "部品コード 'TEST001' が見つかりません";
        Throwable cause = new RuntimeException("Database error");

        // When
        PartNotFoundException exception = new PartNotFoundException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    /**
     * DuplicatePartCodeExceptionのテスト
     * 要件: 1.2
     */
    @Test
    void duplicatePartCodeException_WithMessage_ShouldCreateCorrectException() {
        // Given
        String message = "部品コード 'TEST001' は既に存在します";

        // When
        DuplicatePartCodeException exception = new DuplicatePartCodeException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void duplicatePartCodeException_WithMessageAndCause_ShouldCreateCorrectException() {
        // Given
        String message = "部品コード 'TEST001' は既に存在します";
        Throwable cause = new RuntimeException("Constraint violation");

        // When
        DuplicatePartCodeException exception = new DuplicatePartCodeException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    /**
     * InsufficientStockExceptionのテスト
     * 要件: 5.5
     */
    @Test
    void insufficientStockException_WithMessage_ShouldCreateCorrectException() {
        // Given
        String message = "在庫数量が不足しています。現在の在庫: 5, 要求数量: 10";

        // When
        InsufficientStockException exception = new InsufficientStockException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void insufficientStockException_WithMessageAndCause_ShouldCreateCorrectException() {
        // Given
        String message = "在庫数量が不足しています。現在の在庫: 5, 要求数量: 10";
        Throwable cause = new RuntimeException("Stock calculation error");

        // When
        InsufficientStockException exception = new InsufficientStockException(message, cause);

        // Then
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    /**
     * 例外の継承関係テスト
     */
    @Test
    void customExceptions_ShouldExtendRuntimeException() {
        // Given & When
        PartNotFoundException partNotFound = new PartNotFoundException("test");
        DuplicatePartCodeException duplicateCode = new DuplicatePartCodeException("test");
        InsufficientStockException insufficientStock = new InsufficientStockException("test");

        // Then
        assertTrue(partNotFound instanceof RuntimeException);
        assertTrue(duplicateCode instanceof RuntimeException);
        assertTrue(insufficientStock instanceof RuntimeException);
    }

    /**
     * 例外メッセージの null チェックテスト
     */
    @Test
    void customExceptions_ShouldHandleNullMessage() {
        // When & Then
        assertDoesNotThrow(() -> new PartNotFoundException(null));
        assertDoesNotThrow(() -> new DuplicatePartCodeException(null));
        assertDoesNotThrow(() -> new InsufficientStockException(null));
    }

    /**
     * 例外の throw テスト
     */
    @Test
    void partNotFoundException_ShouldBeThrowable() {
        // Given
        String message = "部品が見つかりません";

        // When & Then
        assertThrows(PartNotFoundException.class, () -> {
            throw new PartNotFoundException(message);
        });
    }

    @Test
    void duplicatePartCodeException_ShouldBeThrowable() {
        // Given
        String message = "部品コードが重複しています";

        // When & Then
        assertThrows(DuplicatePartCodeException.class, () -> {
            throw new DuplicatePartCodeException(message);
        });
    }

    @Test
    void insufficientStockException_ShouldBeThrowable() {
        // Given
        String message = "在庫が不足しています";

        // When & Then
        assertThrows(InsufficientStockException.class, () -> {
            throw new InsufficientStockException(message);
        });
    }
}