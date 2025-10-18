package com.example.inventory.service;

import com.example.inventory.dto.ElectronicPartDto;
import com.example.inventory.dto.ElectronicPartMapper;
import com.example.inventory.dto.InventoryAdjustmentDto;
import com.example.inventory.dto.InventoryAdjustmentResponseDto;
import com.example.inventory.dto.InventoryAdjustmentResponseMapper;
import com.example.inventory.dto.InventoryTransactionMapper;
import com.example.inventory.entity.ElectronicPart;
import com.example.inventory.entity.InventoryTransaction;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.exception.PartNotFoundException;
import com.example.inventory.repository.ElectronicPartRepository;
import com.example.inventory.repository.InventoryTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * InventoryServiceの単体テスト
 * 要件: 1.2, 1.3, 2.2, 3.2, 5.3, 5.5
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ElectronicPartRepository electronicPartRepository;

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private ElectronicPartMapper electronicPartMapper;

    @Mock
    private InventoryTransactionMapper inventoryTransactionMapper;

    @Mock
    private InventoryAdjustmentResponseMapper inventoryAdjustmentResponseMapper;

    @InjectMocks
    private InventoryService inventoryService;

    private ElectronicPart testPart;
    private InventoryAdjustmentDto adjustmentDto;
    private InventoryTransaction testTransaction;
    private InventoryAdjustmentResponseDto responseDto;

    @BeforeEach
    void setUp() {
        testPart = new ElectronicPart();
        testPart.setPartCode("TEST-001");
        testPart.setPartName("テスト部品");
        testPart.setCategory("抵抗");
        testPart.setUnitPrice(new BigDecimal("10.50"));
        testPart.setStockQuantity(100);
        testPart.setLowStockThreshold(10);

        adjustmentDto = new InventoryAdjustmentDto();
        adjustmentDto.setPartCode("TEST-001");
        adjustmentDto.setQuantity(20);
        adjustmentDto.setNotes("テスト調整");

        testTransaction = new InventoryTransaction();
        testTransaction.setPartCode("TEST-001");
        testTransaction.setTransactionType(InventoryTransaction.TransactionType.INCREASE);
        testTransaction.setQuantity(20);
        testTransaction.setPreviousStock(100);
        testTransaction.setNewStock(120);
        testTransaction.setTransactionDate(LocalDateTime.now());

        responseDto = new InventoryAdjustmentResponseDto();
        responseDto.setPartCode("TEST-001");
        responseDto.setPreviousStock(100);
        responseDto.setNewStock(120);
        responseDto.setAdjustmentQuantity(20);
    }

    @Test
    void increaseStock_正常ケース() {
        // Given
        when(electronicPartRepository.findByPartCode("TEST-001")).thenReturn(Optional.of(testPart));
        when(electronicPartRepository.save(testPart)).thenReturn(testPart);
        when(inventoryTransactionMapper.createIncreaseTransaction(adjustmentDto, 100, 120))
            .thenReturn(testTransaction);
        when(inventoryTransactionRepository.save(testTransaction)).thenReturn(testTransaction);
        when(inventoryAdjustmentResponseMapper.toResponseDto(testPart, testTransaction))
            .thenReturn(responseDto);

        // When
        InventoryAdjustmentResponseDto result = inventoryService.increaseStock(adjustmentDto);

        // Then
        assertNotNull(result);
        assertEquals("TEST-001", result.getPartCode());
        assertEquals(100, result.getPreviousStock());
        assertEquals(120, result.getNewStock());
        assertEquals(120, testPart.getStockQuantity()); // 在庫が更新されていることを確認
        
        verify(electronicPartRepository).findByPartCode("TEST-001");
        verify(electronicPartRepository).save(testPart);
        verify(inventoryTransactionRepository).save(testTransaction);
    }

    @Test
    void increaseStock_部品が存在しない() {
        // Given
        adjustmentDto.setPartCode("NONEXISTENT");
        when(electronicPartRepository.findByPartCode("NONEXISTENT")).thenReturn(Optional.empty());

        // When & Then
        PartNotFoundException exception = assertThrows(
            PartNotFoundException.class,
            () -> inventoryService.increaseStock(adjustmentDto)
        );
        
        assertEquals("部品コード 'NONEXISTENT' の部品が見つかりません", exception.getMessage());
        verify(electronicPartRepository, never()).save(any());
        verify(inventoryTransactionRepository, never()).save(any());
    }

    @Test
    void decreaseStock_正常ケース() {
        // Given
        testTransaction.setTransactionType(InventoryTransaction.TransactionType.DECREASE);
        testTransaction.setNewStock(80);
        responseDto.setNewStock(80);
        responseDto.setAdjustmentQuantity(-20);

        when(electronicPartRepository.findByPartCode("TEST-001")).thenReturn(Optional.of(testPart));
        when(electronicPartRepository.save(testPart)).thenReturn(testPart);
        when(inventoryTransactionMapper.createDecreaseTransaction(adjustmentDto, 100, 80))
            .thenReturn(testTransaction);
        when(inventoryTransactionRepository.save(testTransaction)).thenReturn(testTransaction);
        when(inventoryAdjustmentResponseMapper.toResponseDto(testPart, testTransaction))
            .thenReturn(responseDto);

        // When
        InventoryAdjustmentResponseDto result = inventoryService.decreaseStock(adjustmentDto);

        // Then
        assertNotNull(result);
        assertEquals("TEST-001", result.getPartCode());
        assertEquals(80, testPart.getStockQuantity()); // 在庫が減少していることを確認
        
        verify(electronicPartRepository).save(testPart);
        verify(inventoryTransactionRepository).save(testTransaction);
    }

    @Test
    void decreaseStock_在庫不足エラー() {
        // Given
        testPart.setStockQuantity(10); // 現在の在庫を10に設定
        adjustmentDto.setQuantity(20); // 20減らそうとする（結果は-10になる）

        when(electronicPartRepository.findByPartCode("TEST-001")).thenReturn(Optional.of(testPart));

        // When & Then
        InsufficientStockException exception = assertThrows(
            InsufficientStockException.class,
            () -> inventoryService.decreaseStock(adjustmentDto)
        );
        
        assertTrue(exception.getMessage().contains("在庫数量が不足しています"));
        assertTrue(exception.getMessage().contains("現在の在庫: 10"));
        assertTrue(exception.getMessage().contains("減少要求: 20"));
        
        verify(electronicPartRepository, never()).save(any());
        verify(inventoryTransactionRepository, never()).save(any());
    }

    @Test
    void decreaseStock_部品が存在しない() {
        // Given
        adjustmentDto.setPartCode("NONEXISTENT");
        when(electronicPartRepository.findByPartCode("NONEXISTENT")).thenReturn(Optional.empty());

        // When & Then
        PartNotFoundException exception = assertThrows(
            PartNotFoundException.class,
            () -> inventoryService.decreaseStock(adjustmentDto)
        );
        
        assertEquals("部品コード 'NONEXISTENT' の部品が見つかりません", exception.getMessage());
    }

    @Test
    void findLowStockParts_正常ケース() {
        // Given
        ElectronicPart lowStockPart = new ElectronicPart();
        lowStockPart.setPartCode("LOW-001");
        lowStockPart.setStockQuantity(5);
        lowStockPart.setLowStockThreshold(10);

        ElectronicPartDto lowStockDto = new ElectronicPartDto();
        lowStockDto.setPartCode("LOW-001");
        lowStockDto.setStockQuantity(5);

        when(electronicPartRepository.findLowStockPartsUsingThreshold())
            .thenReturn(Arrays.asList(lowStockPart));
        when(electronicPartMapper.toDto(lowStockPart)).thenReturn(lowStockDto);

        // When
        List<ElectronicPartDto> result = inventoryService.findLowStockParts();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("LOW-001", result.get(0).getPartCode());
        assertEquals(5, result.get(0).getStockQuantity());
    }

    @Test
    void findLowStockPartsByThreshold_正常ケース() {
        // Given
        ElectronicPart lowStockPart = new ElectronicPart();
        lowStockPart.setPartCode("LOW-001");
        lowStockPart.setStockQuantity(8);

        ElectronicPartDto lowStockDto = new ElectronicPartDto();
        lowStockDto.setPartCode("LOW-001");
        lowStockDto.setStockQuantity(8);

        when(electronicPartRepository.findLowStockParts(10))
            .thenReturn(Arrays.asList(lowStockPart));
        when(electronicPartMapper.toDto(lowStockPart)).thenReturn(lowStockDto);

        // When
        List<ElectronicPartDto> result = inventoryService.findLowStockPartsByThreshold(10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("LOW-001", result.get(0).getPartCode());
    }

    @Test
    void findLowStockPartsByThreshold_無効な閾値() {
        // When & Then - null閾値
        IllegalArgumentException exception1 = assertThrows(
            IllegalArgumentException.class,
            () -> inventoryService.findLowStockPartsByThreshold(null)
        );
        assertEquals("閾値は0以上である必要があります", exception1.getMessage());

        // When & Then - 負の閾値
        IllegalArgumentException exception2 = assertThrows(
            IllegalArgumentException.class,
            () -> inventoryService.findLowStockPartsByThreshold(-1)
        );
        assertEquals("閾値は0以上である必要があります", exception2.getMessage());
    }

    @Test
    void getInventoryTransactionHistory_正常ケース() {
        // Given
        Page<InventoryTransaction> transactionPage = new PageImpl<>(Arrays.asList(testTransaction));
        when(inventoryTransactionRepository.findByPartCodeOrderByTransactionDateDesc(
            eq("TEST-001"), any(Pageable.class))).thenReturn(transactionPage);

        // When
        Page<InventoryTransaction> result = inventoryService.getInventoryTransactionHistory("TEST-001", 0, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("TEST-001", result.getContent().get(0).getPartCode());
    }

    @Test
    void getTotalQuantityByTransactionType_正常ケース() {
        // Given
        when(inventoryTransactionRepository.sumQuantityByPartCodeAndTransactionType(
            "TEST-001", InventoryTransaction.TransactionType.INCREASE)).thenReturn(100L);

        // When
        Long result = inventoryService.getTotalQuantityByTransactionType(
            "TEST-001", InventoryTransaction.TransactionType.INCREASE);

        // Then
        assertEquals(100L, result);
    }

    @Test
    void getTotalQuantityByTransactionType_nullの場合() {
        // Given
        when(inventoryTransactionRepository.sumQuantityByPartCodeAndTransactionType(
            "TEST-001", InventoryTransaction.TransactionType.INCREASE)).thenReturn(null);

        // When
        Long result = inventoryService.getTotalQuantityByTransactionType(
            "TEST-001", InventoryTransaction.TransactionType.INCREASE);

        // Then
        assertEquals(0L, result);
    }

    @Test
    void getTransactionCountByDateRange_正常ケース() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        when(inventoryTransactionRepository.countTransactionsByDateRange(startDate, endDate))
            .thenReturn(50L);

        // When
        Long result = inventoryService.getTransactionCountByDateRange(startDate, endDate);

        // Then
        assertEquals(50L, result);
    }

    @Test
    void getLatestTransactionByPartCode_正常ケース() {
        // Given
        when(inventoryTransactionRepository.findLatestTransactionByPartCode("TEST-001"))
            .thenReturn(testTransaction);

        // When
        InventoryTransaction result = inventoryService.getLatestTransactionByPartCode("TEST-001");

        // Then
        assertNotNull(result);
        assertEquals("TEST-001", result.getPartCode());
        assertEquals(InventoryTransaction.TransactionType.INCREASE, result.getTransactionType());
    }
}