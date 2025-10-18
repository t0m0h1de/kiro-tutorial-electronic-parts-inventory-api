package com.example.inventory.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ErrorResponseクラスの単体テスト
 * 要件: 7.1, 7.3
 */
class ErrorResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * デフォルトコンストラクタのテスト
     */
    @Test
    void defaultConstructor_ShouldSetTimestamp() {
        // When
        ErrorResponse errorResponse = new ErrorResponse();

        // Then
        assertNotNull(errorResponse.getTimestamp());
        assertTrue(errorResponse.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(errorResponse.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    /**
     * パラメータ付きコンストラクタのテスト
     */
    @Test
    void parameterizedConstructor_ShouldSetAllFields() {
        // Given
        int status = 404;
        String error = "Not Found";
        String message = "部品が見つかりません";
        String path = "/api/v1/parts/TEST001";

        // When
        ErrorResponse errorResponse = new ErrorResponse(status, error, message, path);

        // Then
        assertNotNull(errorResponse.getTimestamp());
        assertEquals(status, errorResponse.getStatus());
        assertEquals(error, errorResponse.getError());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(path, errorResponse.getPath());
    }

    /**
     * Getter/Setterのテスト
     */
    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse();
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 400;
        String error = "Bad Request";
        String message = "バリデーションエラー";
        String path = "/api/v1/parts";

        // When
        errorResponse.setTimestamp(timestamp);
        errorResponse.setStatus(status);
        errorResponse.setError(error);
        errorResponse.setMessage(message);
        errorResponse.setPath(path);

        // Then
        assertEquals(timestamp, errorResponse.getTimestamp());
        assertEquals(status, errorResponse.getStatus());
        assertEquals(error, errorResponse.getError());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(path, errorResponse.getPath());
    }

    /**
     * JSON シリアライゼーションのテスト
     * 要件: 7.3 - 統一されたエラー形式の検証
     */
    @Test
    void jsonSerialization_ShouldProduceCorrectFormat() throws JsonProcessingException {
        // Given
        ErrorResponse errorResponse = new ErrorResponse(
                404, 
                "Not Found", 
                "部品が見つかりません", 
                "/api/v1/parts/TEST001"
        );

        // When
        String json = objectMapper.writeValueAsString(errorResponse);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"status\":404"));
        assertTrue(json.contains("\"error\":\"Not Found\""));
        assertTrue(json.contains("\"message\":\"部品が見つかりません\""));
        assertTrue(json.contains("\"path\":\"/api/v1/parts/TEST001\""));
        assertTrue(json.contains("\"timestamp\":"));
    }

    /**
     * JSON デシリアライゼーションのテスト
     */
    @Test
    void jsonDeserialization_ShouldWorkCorrectly() throws JsonProcessingException {
        // Given
        String json = """
                {
                    "timestamp": "2024-01-01T10:00:00",
                    "status": 400,
                    "error": "Bad Request",
                    "message": "バリデーションエラー",
                    "path": "/api/v1/parts"
                }
                """;

        // When
        ErrorResponse errorResponse = objectMapper.readValue(json, ErrorResponse.class);

        // Then
        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("バリデーションエラー", errorResponse.getMessage());
        assertEquals("/api/v1/parts", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    /**
     * タイムスタンプフォーマットのテスト
     * 要件: 7.3 - 統一されたエラー形式
     */
    @Test
    void timestampFormat_ShouldFollowISO8601Pattern() throws JsonProcessingException {
        // Given
        ErrorResponse errorResponse = new ErrorResponse(200, "OK", "Success", "/api/v1/parts");

        // When
        String json = objectMapper.writeValueAsString(errorResponse);

        // Then
        // タイムスタンプが正しい形式（yyyy-MM-dd'T'HH:mm:ss）であることを確認
        assertTrue(json.matches(".*\"timestamp\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\".*"));
    }

    /**
     * 必須フィールドの存在確認テスト
     * 要件: 7.3 - エラーレスポンス形式の検証
     */
    @Test
    void errorResponse_ShouldContainAllRequiredFields() {
        // Given
        ErrorResponse errorResponse = new ErrorResponse(
                500, 
                "Internal Server Error", 
                "システムエラー", 
                "/api/v1/parts"
        );

        // Then - すべての必須フィールドが存在することを確認
        assertNotNull(errorResponse.getTimestamp(), "timestamp should not be null");
        assertTrue(errorResponse.getStatus() > 0, "status should be positive");
        assertNotNull(errorResponse.getError(), "error should not be null");
        assertNotNull(errorResponse.getMessage(), "message should not be null");
        assertNotNull(errorResponse.getPath(), "path should not be null");
    }
}