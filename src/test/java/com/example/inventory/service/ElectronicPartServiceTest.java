package com.example.inventory.service;

import com.example.inventory.dto.ElectronicPartDto;
import com.example.inventory.dto.ElectronicPartMapper;
import com.example.inventory.dto.SearchCriteriaDto;
import com.example.inventory.entity.ElectronicPart;
import com.example.inventory.exception.DuplicatePartCodeException;
import com.example.inventory.exception.PartNotFoundException;
import com.example.inventory.repository.ElectronicPartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ElectronicPartServiceの単体テスト
 * 要件: 1.2, 1.3, 2.2, 3.2, 5.3, 5.5
 */
@ExtendWith(MockitoExtension.class)
class ElectronicPartServiceTest {

    @Mock
    private ElectronicPartRepository electronicPartRepository;

    @Mock
    private ElectronicPartMapper electronicPartMapper;

    @InjectMocks
    private ElectronicPartService electronicPartService;

    private ElectronicPartDto testDto;
    private ElectronicPart testEntity;

    @BeforeEach
    void setUp() {
        testDto = new ElectronicPartDto();
        testDto.setPartCode("TEST-001");
        testDto.setPartName("テスト部品");
        testDto.setCategory("抵抗");
        testDto.setSpecifications("1kΩ");
        testDto.setUnitPrice(new BigDecimal("10.50"));
        testDto.setStockQuantity(100);
        testDto.setLowStockThreshold(10);

        testEntity = new ElectronicPart();
        testEntity.setPartCode("TEST-001");
        testEntity.setPartName("テスト部品");
        testEntity.setCategory("抵抗");
        testEntity.setSpecifications("1kΩ");
        testEntity.setUnitPrice(new BigDecimal("10.50"));
        testEntity.setStockQuantity(100);
        testEntity.setLowStockThreshold(10);
    }

    @Test
    void createElectronicPart_正常ケース() {
        // Given
        when(electronicPartRepository.existsByPartCode("TEST-001")).thenReturn(false);
        when(electronicPartMapper.toEntity(testDto)).thenReturn(testEntity);
        when(electronicPartRepository.save(testEntity)).thenReturn(testEntity);
        when(electronicPartMapper.toDto(testEntity)).thenReturn(testDto);

        // When
        ElectronicPartDto result = electronicPartService.createElectronicPart(testDto);

        // Then
        assertNotNull(result);
        assertEquals("TEST-001", result.getPartCode());
        assertEquals("テスト部品", result.getPartName());
        verify(electronicPartRepository).existsByPartCode("TEST-001");
        verify(electronicPartRepository).save(testEntity);
    }

    @Test
    void createElectronicPart_部品コード重複エラー() {
        // Given
        when(electronicPartRepository.existsByPartCode("TEST-001")).thenReturn(true);

        // When & Then
        DuplicatePartCodeException exception = assertThrows(
            DuplicatePartCodeException.class,
            () -> electronicPartService.createElectronicPart(testDto)
        );
        
        assertEquals("部品コード 'TEST-001' は既に存在します", exception.getMessage());
        verify(electronicPartRepository).existsByPartCode("TEST-001");
        verify(electronicPartRepository, never()).save(any());
    }

    @Test
    void getElectronicPartByCode_正常ケース() {
        // Given
        when(electronicPartRepository.findByPartCode("TEST-001")).thenReturn(Optional.of(testEntity));
        when(electronicPartMapper.toDto(testEntity)).thenReturn(testDto);

        // When
        ElectronicPartDto result = electronicPartService.getElectronicPartByCode("TEST-001");

        // Then
        assertNotNull(result);
        assertEquals("TEST-001", result.getPartCode());
        verify(electronicPartRepository).findByPartCode("TEST-001");
    }

    @Test
    void getElectronicPartByCode_部品が存在しない() {
        // Given
        when(electronicPartRepository.findByPartCode("NONEXISTENT")).thenReturn(Optional.empty());

        // When & Then
        PartNotFoundException exception = assertThrows(
            PartNotFoundException.class,
            () -> electronicPartService.getElectronicPartByCode("NONEXISTENT")
        );
        
        assertEquals("部品コード 'NONEXISTENT' の部品が見つかりません", exception.getMessage());
    }

    @Test
    void updateElectronicPart_正常ケース() {
        // Given
        ElectronicPartDto updateDto = new ElectronicPartDto();
        updateDto.setPartCode("TEST-001");
        updateDto.setPartName("更新された部品");
        updateDto.setCategory("コンデンサ");
        updateDto.setUnitPrice(new BigDecimal("15.00"));
        updateDto.setStockQuantity(150);
        updateDto.setLowStockThreshold(15);

        when(electronicPartRepository.findByPartCode("TEST-001")).thenReturn(Optional.of(testEntity));
        when(electronicPartRepository.save(testEntity)).thenReturn(testEntity);
        when(electronicPartMapper.toDto(testEntity)).thenReturn(updateDto);

        // When
        ElectronicPartDto result = electronicPartService.updateElectronicPart("TEST-001", updateDto);

        // Then
        assertNotNull(result);
        assertEquals("TEST-001", result.getPartCode());
        verify(electronicPartMapper).updateEntityFromDto(testEntity, updateDto);
        verify(electronicPartRepository).save(testEntity);
    }

    @Test
    void updateElectronicPart_部品が存在しない() {
        // Given
        when(electronicPartRepository.findByPartCode("NONEXISTENT")).thenReturn(Optional.empty());

        // When & Then
        PartNotFoundException exception = assertThrows(
            PartNotFoundException.class,
            () -> electronicPartService.updateElectronicPart("NONEXISTENT", testDto)
        );
        
        assertEquals("部品コード 'NONEXISTENT' の部品が見つかりません", exception.getMessage());
    }

    @Test
    void deleteElectronicPart_正常ケース() {
        // Given
        when(electronicPartRepository.existsByPartCode("TEST-001")).thenReturn(true);

        // When
        electronicPartService.deleteElectronicPart("TEST-001");

        // Then
        verify(electronicPartRepository).existsByPartCode("TEST-001");
        verify(electronicPartRepository).deleteByPartCode("TEST-001");
    }

    @Test
    void deleteElectronicPart_部品が存在しない() {
        // Given
        when(electronicPartRepository.existsByPartCode("NONEXISTENT")).thenReturn(false);

        // When & Then
        PartNotFoundException exception = assertThrows(
            PartNotFoundException.class,
            () -> electronicPartService.deleteElectronicPart("NONEXISTENT")
        );
        
        assertEquals("部品コード 'NONEXISTENT' の部品が見つかりません", exception.getMessage());
        verify(electronicPartRepository, never()).deleteByPartCode(any());
    }

    @Test
    void searchElectronicParts_正常ケース() {
        // Given
        SearchCriteriaDto searchCriteria = new SearchCriteriaDto();
        searchCriteria.setPartName("テスト");
        searchCriteria.setCategory("抵抗");
        searchCriteria.setMinStock(0);
        searchCriteria.setMaxStock(200);
        searchCriteria.setPage(0);
        searchCriteria.setSize(10);

        Page<ElectronicPart> entityPage = new PageImpl<>(Arrays.asList(testEntity));
        when(electronicPartRepository.findBySearchCriteria(
            eq("テスト"), eq("抵抗"), eq(0), eq(200), any(Pageable.class)))
            .thenReturn(entityPage);
        when(electronicPartMapper.toDto(testEntity)).thenReturn(testDto);

        // When
        Page<ElectronicPartDto> result = electronicPartService.searchElectronicParts(searchCriteria);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("TEST-001", result.getContent().get(0).getPartCode());
    }

    @Test
    void searchElectronicParts_無効な在庫範囲() {
        // Given
        SearchCriteriaDto searchCriteria = new SearchCriteriaDto();
        searchCriteria.setMinStock(100);
        searchCriteria.setMaxStock(50); // 最小 > 最大

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> electronicPartService.searchElectronicParts(searchCriteria)
        );
        
        assertEquals("最小在庫数は最大在庫数以下である必要があります", exception.getMessage());
    }

    @Test
    void getAllElectronicParts_正常ケース() {
        // Given
        Page<ElectronicPart> entityPage = new PageImpl<>(Arrays.asList(testEntity));
        when(electronicPartRepository.findAll(any(Pageable.class))).thenReturn(entityPage);
        when(electronicPartMapper.toDto(testEntity)).thenReturn(testDto);

        // When
        Page<ElectronicPartDto> result = electronicPartService.getAllElectronicParts(0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(electronicPartRepository).findAll(any(Pageable.class));
    }

    @Test
    void existsByPartCode_存在する場合() {
        // Given
        when(electronicPartRepository.existsByPartCode("TEST-001")).thenReturn(true);

        // When
        boolean result = electronicPartService.existsByPartCode("TEST-001");

        // Then
        assertTrue(result);
    }

    @Test
    void existsByPartCode_存在しない場合() {
        // Given
        when(electronicPartRepository.existsByPartCode("NONEXISTENT")).thenReturn(false);

        // When
        boolean result = electronicPartService.existsByPartCode("NONEXISTENT");

        // Then
        assertFalse(result);
    }

    @Test
    void getTotalStockQuantity_正常ケース() {
        // Given
        when(electronicPartRepository.getTotalStockQuantity()).thenReturn(1000L);

        // When
        Long result = electronicPartService.getTotalStockQuantity();

        // Then
        assertEquals(1000L, result);
    }

    @Test
    void getTotalStockQuantity_nullの場合() {
        // Given
        when(electronicPartRepository.getTotalStockQuantity()).thenReturn(null);

        // When
        Long result = electronicPartService.getTotalStockQuantity();

        // Then
        assertEquals(0L, result);
    }
}