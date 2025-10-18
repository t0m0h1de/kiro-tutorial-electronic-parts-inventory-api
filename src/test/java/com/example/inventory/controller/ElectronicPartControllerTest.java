package com.example.inventory.controller;

import com.example.inventory.dto.ElectronicPartDto;
import com.example.inventory.dto.SearchCriteriaDto;
import com.example.inventory.exception.DuplicatePartCodeException;
import com.example.inventory.exception.PartNotFoundException;
import com.example.inventory.service.ElectronicPartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ElectronicPartControllerの単体テスト
 * MockMvcを使用したAPIエンドポイントのテスト
 */
@WebMvcTest(ElectronicPartController.class)
class ElectronicPartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ElectronicPartService electronicPartService;

    @Autowired
    private ObjectMapper objectMapper;

    private ElectronicPartDto testPartDto;

    @BeforeEach
    void setUp() {
        testPartDto = new ElectronicPartDto(
                "R001",
                "抵抗器 1kΩ",
                "抵抗",
                "1/4W 5%",
                new BigDecimal("10.50"),
                100,
                10
        );
    }

    @Test
    void createElectronicPart_ValidRequest_ReturnsCreated() throws Exception {
        // Given
        when(electronicPartService.createElectronicPart(any(ElectronicPartDto.class)))
                .thenReturn(testPartDto);

        // When & Then
        mockMvc.perform(post("/api/v1/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPartDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.partCode").value("R001"))
                .andExpect(jsonPath("$.partName").value("抵抗器 1kΩ"))
                .andExpect(jsonPath("$.category").value("抵抗"))
                .andExpect(jsonPath("$.unitPrice").value(10.50))
                .andExpect(jsonPath("$.stockQuantity").value(100))
                .andExpect(jsonPath("$.lowStockThreshold").value(10));

        verify(electronicPartService).createElectronicPart(any(ElectronicPartDto.class));
    }

    @Test
    void createElectronicPart_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Given - 必須フィールドが空のDTO
        ElectronicPartDto invalidDto = new ElectronicPartDto();

        // When & Then
        mockMvc.perform(post("/api/v1/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(electronicPartService, never()).createElectronicPart(any());
    }

    @Test
    void createElectronicPart_DuplicatePartCode_ReturnsConflict() throws Exception {
        // Given
        when(electronicPartService.createElectronicPart(any(ElectronicPartDto.class)))
                .thenThrow(new DuplicatePartCodeException("部品コード 'R001' は既に存在します"));

        // When & Then
        mockMvc.perform(post("/api/v1/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPartDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("部品コード 'R001' は既に存在します"));
    }

    @Test
    void getElectronicPart_ExistingPart_ReturnsOk() throws Exception {
        // Given
        when(electronicPartService.getElectronicPartByCode("R001"))
                .thenReturn(testPartDto);

        // When & Then
        mockMvc.perform(get("/api/v1/parts/R001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partCode").value("R001"))
                .andExpect(jsonPath("$.partName").value("抵抗器 1kΩ"));

        verify(electronicPartService).getElectronicPartByCode("R001");
    }

    @Test
    void getElectronicPart_NonExistingPart_ReturnsNotFound() throws Exception {
        // Given
        when(electronicPartService.getElectronicPartByCode("INVALID"))
                .thenThrow(new PartNotFoundException("部品コード 'INVALID' の部品が見つかりません"));

        // When & Then
        mockMvc.perform(get("/api/v1/parts/INVALID"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("部品コード 'INVALID' の部品が見つかりません"));
    }

    @Test
    void updateElectronicPart_ValidRequest_ReturnsOk() throws Exception {
        // Given
        ElectronicPartDto updatedDto = new ElectronicPartDto(
                "R001",
                "抵抗器 2kΩ",
                "抵抗",
                "1/4W 5%",
                new BigDecimal("12.00"),
                150,
                15
        );
        when(electronicPartService.updateElectronicPart(eq("R001"), any(ElectronicPartDto.class)))
                .thenReturn(updatedDto);

        // When & Then
        mockMvc.perform(put("/api/v1/parts/R001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partName").value("抵抗器 2kΩ"))
                .andExpect(jsonPath("$.unitPrice").value(12.00))
                .andExpect(jsonPath("$.stockQuantity").value(150));

        verify(electronicPartService).updateElectronicPart(eq("R001"), any(ElectronicPartDto.class));
    }

    @Test
    void updateElectronicPart_NonExistingPart_ReturnsNotFound() throws Exception {
        // Given
        when(electronicPartService.updateElectronicPart(eq("INVALID"), any(ElectronicPartDto.class)))
                .thenThrow(new PartNotFoundException("部品コード 'INVALID' の部品が見つかりません"));

        // When & Then
        mockMvc.perform(put("/api/v1/parts/INVALID")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPartDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteElectronicPart_ExistingPart_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(electronicPartService).deleteElectronicPart("R001");

        // When & Then
        mockMvc.perform(delete("/api/v1/parts/R001"))
                .andExpect(status().isNoContent());

        verify(electronicPartService).deleteElectronicPart("R001");
    }

    @Test
    void deleteElectronicPart_NonExistingPart_ReturnsNotFound() throws Exception {
        // Given
        doThrow(new PartNotFoundException("部品コード 'INVALID' の部品が見つかりません"))
                .when(electronicPartService).deleteElectronicPart("INVALID");

        // When & Then
        mockMvc.perform(delete("/api/v1/parts/INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchElectronicParts_WithoutParameters_ReturnsAllParts() throws Exception {
        // Given
        Page<ElectronicPartDto> page = new PageImpl<>(Arrays.asList(testPartDto), PageRequest.of(0, 20), 1);
        when(electronicPartService.searchElectronicParts(any(SearchCriteriaDto.class)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/parts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].partCode").value("R001"));

        verify(electronicPartService).searchElectronicParts(any(SearchCriteriaDto.class));
    }

    @Test
    void searchElectronicParts_WithSearchParameters_ReturnsFilteredResults() throws Exception {
        // Given
        Page<ElectronicPartDto> page = new PageImpl<>(Arrays.asList(testPartDto), PageRequest.of(0, 10), 1);
        when(electronicPartService.searchElectronicParts(any(SearchCriteriaDto.class)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/parts")
                        .param("partName", "抵抗")
                        .param("category", "抵抗")
                        .param("minStock", "50")
                        .param("maxStock", "200")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].partCode").value("R001"));
    }

    @Test
    void searchElectronicParts_NoResults_ReturnsEmptyPage() throws Exception {
        // Given
        Page<ElectronicPartDto> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
        when(electronicPartService.searchElectronicParts(any(SearchCriteriaDto.class)))
                .thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/v1/parts")
                        .param("partName", "存在しない部品"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}