package com.example.inventory.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * GlobalExceptionHandlerの単体テスト
 * 要件: 7.1, 7.2, 7.3, 7.4
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private WebRequest webRequest;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        webRequest = mock(WebRequest.class);
        objectMapper = new ObjectMapper();
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/parts");
    }

    /**
     * PartNotFoundExceptionの処理テスト
     * 要件: 1.2, 2.2, 3.2, 5.5
     */
    @Test
    void handlePartNotFoundException_ShouldReturn404WithCorrectErrorResponse() {
        // Given
        String errorMessage = "部品コード 'TEST001' が見つかりません";
        PartNotFoundException exception = new PartNotFoundException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handlePartNotFoundException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(404, errorResponse.getStatus());
        assertEquals("Not Found", errorResponse.getError());
        assertEquals(errorMessage, errorResponse.getMessage());
        assertEquals("/api/v1/parts", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    /**
     * DuplicatePartCodeExceptionの処理テスト
     * 要件: 1.2
     */
    @Test
    void handleDuplicatePartCodeException_ShouldReturn409WithCorrectErrorResponse() {
        // Given
        String errorMessage = "部品コード 'TEST001' は既に存在します";
        DuplicatePartCodeException exception = new DuplicatePartCodeException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleDuplicatePartCodeException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(409, errorResponse.getStatus());
        assertEquals("Conflict", errorResponse.getError());
        assertEquals(errorMessage, errorResponse.getMessage());
        assertEquals("/api/v1/parts", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    /**
     * InsufficientStockExceptionの処理テスト
     * 要件: 5.5
     */
    @Test
    void handleInsufficientStockException_ShouldReturn400WithCorrectErrorResponse() {
        // Given
        String errorMessage = "在庫数量が不足しています。現在の在庫: 5, 要求数量: 10";
        InsufficientStockException exception = new InsufficientStockException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleInsufficientStockException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals(errorMessage, errorResponse.getMessage());
        assertEquals("/api/v1/parts", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    /**
     * MethodArgumentNotValidExceptionの処理テスト
     * 要件: 1.3, 7.1
     */
    @Test
    void handleValidationExceptions_ShouldReturn400WithValidationErrors() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError1 = new FieldError("electronicPartDto", "partCode", "部品コードは必須です");
        FieldError fieldError2 = new FieldError("electronicPartDto", "partName", "部品名は必須です");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleValidationExceptions(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertTrue(errorResponse.getMessage().contains("バリデーションエラー"));
        assertTrue(errorResponse.getMessage().contains("partCode"));
        assertTrue(errorResponse.getMessage().contains("partName"));
        assertEquals("/api/v1/parts", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    /**
     * IllegalArgumentExceptionの処理テスト
     * 要件: 7.1
     */
    @Test
    void handleIllegalArgumentException_ShouldReturn400WithCorrectErrorResponse() {
        // Given
        String errorMessage = "不正な引数が指定されました";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleIllegalArgumentException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals(errorMessage, errorResponse.getMessage());
        assertEquals("/api/v1/parts", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    /**
     * DataAccessExceptionの処理テスト
     * 要件: 7.2
     */
    @Test
    void handleDataAccessException_ShouldReturn500WithCorrectErrorResponse() {
        // Given
        DataAccessException exception = mock(DataAccessException.class);
        when(exception.getMessage()).thenReturn("Database connection failed");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleDataAccessException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(500, errorResponse.getStatus());
        assertEquals("Internal Server Error", errorResponse.getError());
        assertEquals("データベースアクセスエラーが発生しました", errorResponse.getMessage());
        assertEquals("/api/v1/parts", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    /**
     * 予期しない例外の処理テスト
     * 要件: 7.2, 7.4
     */
    @Test
    void handleGenericException_ShouldReturn500WithCorrectErrorResponse() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error occurred");

        // When
        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleGenericException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(500, errorResponse.getStatus());
        assertEquals("Internal Server Error", errorResponse.getError());
        assertEquals("システムエラーが発生しました", errorResponse.getMessage());
        assertEquals("/api/v1/parts", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    /**
     * エラーレスポンス形式の統一性テスト
     * 要件: 7.3
     */
    @Test
    void allExceptionHandlers_ShouldReturnConsistentErrorResponseFormat() {
        // Given
        PartNotFoundException partNotFound = new PartNotFoundException("Test message");
        DuplicatePartCodeException duplicateCode = new DuplicatePartCodeException("Test message");
        InsufficientStockException insufficientStock = new InsufficientStockException("Test message");

        // When
        ResponseEntity<ErrorResponse> response1 = globalExceptionHandler
                .handlePartNotFoundException(partNotFound, webRequest);
        ResponseEntity<ErrorResponse> response2 = globalExceptionHandler
                .handleDuplicatePartCodeException(duplicateCode, webRequest);
        ResponseEntity<ErrorResponse> response3 = globalExceptionHandler
                .handleInsufficientStockException(insufficientStock, webRequest);

        // Then - すべてのレスポンスが同じ形式を持つことを確認
        assertErrorResponseFormat(response1.getBody());
        assertErrorResponseFormat(response2.getBody());
        assertErrorResponseFormat(response3.getBody());
    }

    /**
     * エラーレスポンスの形式を検証するヘルパーメソッド
     */
    private void assertErrorResponseFormat(ErrorResponse errorResponse) {
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getTimestamp());
        assertTrue(errorResponse.getStatus() > 0);
        assertNotNull(errorResponse.getError());
        assertNotNull(errorResponse.getMessage());
        assertNotNull(errorResponse.getPath());
    }
}