package com.example.inventory.controller;

import com.example.inventory.dto.ElectronicPartDto;
import com.example.inventory.dto.InventoryAdjustmentDto;
import com.example.inventory.dto.InventoryAdjustmentResponseDto;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.exception.PartNotFoundException;
import com.example.inventory.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * InventoryControllerの単体テスト
 * MockMvcを使用したAPIエンドポイントのテスト
 */
@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private InventoryAdjustmentDto testAdjustmentDto;
    private InventoryAdjustmentResponseDto testResponseDto;
    private ElectronicPartDto testPartDto;

    @BeforeEach
    void setUp() {
        testAdjustmentDto = new InventoryAdjustmentDto(
                "R001",
                50,
                "入荷による在庫増加"
        );

        testResponseDto = new InventoryAdjustmentResponseDto(
                "R001",
                "抵抗器 1kΩ",
                100,
                150,
                50,
                "INCREASE",
                LocalDateTime.now(),
                "入荷による在庫増加"
        );

        testPartDto = new ElectronicPartDto(
                "R001",
                "抵抗器 1kΩ",
                "抵抗",
                "1/4W 5%",
                new BigDecimal("10.50"),
                5,
                10
        );
    }

    @Test
    void increaseStock_ValidRequest_ReturnsOk() throws Exception {
        // Given
        when(inventoryService.increaseStock(any(InventoryAdjustmentDto.class)))
                .thenReturn(testResponseDto);

        // When & Then
        mockMvc.perform(put("/api/v1/parts/R001/inventory/increase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAdjustmentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partCode").value("R001"))
                .andExpect(jsonPath("$.partName").value("抵抗器 1kΩ"))
                .andExpect(jsonPath("$.previousStock").value(100))
                .andExpect(jsonPath("$.newStock").value(150))
                .andExpect(jsonPath("$.adjustmentQuantity").value(50))
                .andExpect(jsonPath("$.adjustmentType").value("INCREASE"));

        verify(inventoryService).increaseStock(any(InventoryAdjustmentDto.class));
    }

    @Test
    void increaseStock_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given - 必須フィールドが空のDTO
        InventoryAdjustmentDto invalidDto = new InventoryAdjustmentDto();

        // When & Then
        mockMvc.perform(put("/api/v1/parts/R001/inventory/increase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(inventoryService, never()).increaseStock(any());
    }

    @Test
    void increaseStock_NonExistingPart_ReturnsNotFound() throws Exception {
        // Given
        when(inventoryService.increaseStock(any(InventoryAdjustmentDto.class)))
                .thenThrow(new PartNotFoundException("部品コード 'INVALID' の部品が見つかりません"));

        // When & Then
        mockMvc.perform(put("/api/v1/parts/INVALID/inventory/increase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAdjustmentDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("部品コード 'INVALID' の部品が見つかりません"));
    }

    @Test
    void decreaseStock_ValidRequest_ReturnsOk() throws Exception {
        // Given
        InventoryAdjustmentResponseDto decreaseResponse = new InventoryAdjustmentResponseDto(
                "R001",
                "抵抗器 1kΩ",
                150,
                100,
                50,
                "DECREASE",
                LocalDateTime.now(),
                "出荷による在庫減少"
        );
        when(inventoryService.decreaseStock(any(InventoryAdjustmentDto.class)))
                .thenReturn(decreaseResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/parts/R001/inventory/decrease")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAdjustmentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partCode").value("R001"))
                .andExpect(jsonPath("$.adjustmentType").value("DECREASE"))
                .andExpect(jsonPath("$.previousStock").value(150))
                .andExpect(jsonPath("$.newStock").value(100));

        verify(inventoryService).decreaseStock(any(InventoryAdjustmentDto.class));
    }

    @Test
    void decreaseStock_InsufficientStock_ReturnsBadRequest() throws Exception {
        // Given
        when(inventoryService.decreaseStock(any(InventoryAdjustmentDto.class)))
                .thenThrow(new InsufficientStockException("在庫が不足しています。現在の在庫: 10, 要求数量: 50"));

        // When & Then
        mockMvc.perform(put("/api/v1/parts/R001/inventory/decrease")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAdjustmentDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("在庫が不足しています。現在の在庫: 10, 要求数量: 50"));
    }

    @Test
    void decreaseStock_NonExistingPart_ReturnsNotFound() throws Exception {
        // Given
        when(inventoryService.decreaseStock(any(InventoryAdjustmentDto.class)))
                .thenThrow(new PartNotFoundException("部品コード 'INVALID' の部品が見つかりません"));

        // When & Then
        mockMvc.perform(put("/api/v1/parts/INVALID/inventory/decrease")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAdjustmentDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLowStockParts_WithoutThreshold_ReturnsLowStockParts() throws Exception {
        // Given
        List<ElectronicPartDto> lowStockParts = Arrays.asList(testPartDto);
        when(inventoryService.findLowStockParts())
                .thenReturn(lowStockParts);

        // When & Then
        mockMvc.perform(get("/api/v1/parts/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].partCode").value("R001"))
                .andExpect(jsonPath("$[0].stockQuantity").value(5))
                .andExpect(jsonPath("$[0].lowStockThreshold").value(10));

        verify(inventoryService).findLowStockParts();
        verify(inventoryService, never()).findLowStockPartsByThreshold(anyInt());
    }

    @Test
    void getLowStockParts_WithThreshold_ReturnsFilteredLowStockParts() throws Exception {
        // Given
        List<ElectronicPartDto> lowStockParts = Arrays.asList(testPartDto);
        when(inventoryService.findLowStockPartsByThreshold(15))
                .thenReturn(lowStockParts);

        // When & Then
        mockMvc.perform(get("/api/v1/parts/low-stock")
                        .param("threshold", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].partCode").value("R001"));

        verify(inventoryService).findLowStockPartsByThreshold(15);
        verify(inventoryService, never()).findLowStockParts();
    }

    @Test
    void getLowStockParts_NoLowStockParts_ReturnsEmptyList() throws Exception {
        // Given
        when(inventoryService.findLowStockParts())
                .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/parts/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(inventoryService).findLowStockParts();
    }

    @Test
    void getLowStockParts_WithZeroThreshold_ReturnsAllParts() throws Exception {
        // Given
        List<ElectronicPartDto> allParts = Arrays.asList(testPartDto);
        when(inventoryService.findLowStockPartsByThreshold(0))
                .thenReturn(allParts);

        // When & Then
        mockMvc.perform(get("/api/v1/parts/low-stock")
                        .param("threshold", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].partCode").value("R001"));

        verify(inventoryService).findLowStockPartsByThreshold(0);
    }
}