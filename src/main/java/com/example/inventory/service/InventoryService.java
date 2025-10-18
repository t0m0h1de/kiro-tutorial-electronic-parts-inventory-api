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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 在庫管理サービスクラス
 * 在庫増減、低在庫部品検索、在庫変動履歴の記録を提供
 */
@Service
@Transactional
public class InventoryService {

    private final ElectronicPartRepository electronicPartRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final ElectronicPartMapper electronicPartMapper;
    private final InventoryTransactionMapper inventoryTransactionMapper;
    private final InventoryAdjustmentResponseMapper inventoryAdjustmentResponseMapper;

    @Autowired
    public InventoryService(ElectronicPartRepository electronicPartRepository,
                          InventoryTransactionRepository inventoryTransactionRepository,
                          ElectronicPartMapper electronicPartMapper,
                          InventoryTransactionMapper inventoryTransactionMapper,
                          InventoryAdjustmentResponseMapper inventoryAdjustmentResponseMapper) {
        this.electronicPartRepository = electronicPartRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.electronicPartMapper = electronicPartMapper;
        this.inventoryTransactionMapper = inventoryTransactionMapper;
        this.inventoryAdjustmentResponseMapper = inventoryAdjustmentResponseMapper;
    }

    /**
     * 在庫数量を増加
     * 要件: 5.1, 5.4
     * @param adjustmentDto 在庫調整情報
     * @return 在庫調整結果
     * @throws PartNotFoundException 部品が存在しない場合
     */
    public InventoryAdjustmentResponseDto increaseStock(InventoryAdjustmentDto adjustmentDto) {
        ElectronicPart part = electronicPartRepository.findByPartCode(adjustmentDto.getPartCode())
                .orElseThrow(() -> new PartNotFoundException("部品コード '" + adjustmentDto.getPartCode() + "' の部品が見つかりません"));

        Integer previousStock = part.getStockQuantity();
        Integer newStock = previousStock + adjustmentDto.getQuantity();
        
        // 在庫数量を更新
        part.setStockQuantity(newStock);
        electronicPartRepository.save(part);

        // 在庫変動履歴を記録
        InventoryTransaction transaction = inventoryTransactionMapper.createIncreaseTransaction(
                adjustmentDto, previousStock, newStock);
        inventoryTransactionRepository.save(transaction);

        return inventoryAdjustmentResponseMapper.toResponseDto(part, transaction);
    }

    /**
     * 在庫数量を減少
     * 要件: 5.2, 5.3, 5.4, 5.5
     * @param adjustmentDto 在庫調整情報
     * @return 在庫調整結果
     * @throws PartNotFoundException 部品が存在しない場合
     * @throws InsufficientStockException 在庫数量が不足している場合
     */
    public InventoryAdjustmentResponseDto decreaseStock(InventoryAdjustmentDto adjustmentDto) {
        ElectronicPart part = electronicPartRepository.findByPartCode(adjustmentDto.getPartCode())
                .orElseThrow(() -> new PartNotFoundException("部品コード '" + adjustmentDto.getPartCode() + "' の部品が見つかりません"));

        Integer previousStock = part.getStockQuantity();
        Integer newStock = previousStock - adjustmentDto.getQuantity();

        // 在庫数量のマイナスチェック（要件5.3）
        if (newStock < 0) {
            throw new InsufficientStockException("在庫数量が不足しています。現在の在庫: " + previousStock + 
                                               ", 減少要求: " + adjustmentDto.getQuantity());
        }

        // 在庫数量を更新
        part.setStockQuantity(newStock);
        electronicPartRepository.save(part);

        // 在庫変動履歴を記録
        InventoryTransaction transaction = inventoryTransactionMapper.createDecreaseTransaction(
                adjustmentDto, previousStock, newStock);
        inventoryTransactionRepository.save(transaction);

        return inventoryAdjustmentResponseMapper.toResponseDto(part, transaction);
    }

    /**
     * 低在庫部品を検索（各部品の低在庫閾値を使用）
     * 要件: 6.1, 6.2, 6.3
     * @return 低在庫部品のリスト（在庫数量昇順）
     */
    @Transactional(readOnly = true)
    public List<ElectronicPartDto> findLowStockParts() {
        List<ElectronicPart> lowStockParts = electronicPartRepository.findLowStockPartsUsingThreshold();
        return lowStockParts.stream()
                .map(electronicPartMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 指定された閾値以下の低在庫部品を検索
     * 要件: 6.1, 6.2, 6.3
     * @param threshold 在庫閾値
     * @return 低在庫部品のリスト（在庫数量昇順）
     */
    @Transactional(readOnly = true)
    public List<ElectronicPartDto> findLowStockPartsByThreshold(Integer threshold) {
        if (threshold == null || threshold < 0) {
            throw new IllegalArgumentException("閾値は0以上である必要があります");
        }

        List<ElectronicPart> lowStockParts = electronicPartRepository.findLowStockParts(threshold);
        return lowStockParts.stream()
                .map(electronicPartMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 部品コードで在庫変動履歴を取得
     * @param partCode 部品コード
     * @param page ページ番号
     * @param size ページサイズ
     * @return 在庫変動履歴のPage
     */
    @Transactional(readOnly = true)
    public Page<InventoryTransaction> getInventoryTransactionHistory(String partCode, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        return inventoryTransactionRepository.findByPartCodeOrderByTransactionDateDesc(partCode, pageable);
    }

    /**
     * 全ての在庫変動履歴を取得
     * @param page ページ番号
     * @param size ページサイズ
     * @return 在庫変動履歴のPage
     */
    @Transactional(readOnly = true)
    public Page<InventoryTransaction> getAllInventoryTransactionHistory(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        return inventoryTransactionRepository.findAllByOrderByTransactionDateDesc(pageable);
    }

    /**
     * 期間指定で在庫変動履歴を取得
     * @param startDate 開始日時
     * @param endDate 終了日時
     * @param page ページ番号
     * @param size ページサイズ
     * @return 在庫変動履歴のPage
     */
    @Transactional(readOnly = true)
    public Page<InventoryTransaction> getInventoryTransactionHistoryByDateRange(
            LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
        return inventoryTransactionRepository.findByTransactionDateBetweenOrderByTransactionDateDesc(
                startDate, endDate, pageable);
    }

    /**
     * 部品コード別の在庫変動回数を取得
     * @return 部品コード別変動回数のリスト
     */
    @Transactional(readOnly = true)
    public List<Object[]> getTransactionCountByPartCode() {
        return inventoryTransactionRepository.countTransactionsByPartCode();
    }

    /**
     * 期間内の在庫変動総数を取得
     * @param startDate 開始日時
     * @param endDate 終了日時
     * @return 在庫変動総数
     */
    @Transactional(readOnly = true)
    public Long getTransactionCountByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return inventoryTransactionRepository.countTransactionsByDateRange(startDate, endDate);
    }

    /**
     * 部品の最新在庫変動履歴を取得
     * @param partCode 部品コード
     * @return 最新の在庫変動履歴
     */
    @Transactional(readOnly = true)
    public InventoryTransaction getLatestTransactionByPartCode(String partCode) {
        return inventoryTransactionRepository.findLatestTransactionByPartCode(partCode);
    }

    /**
     * 取引種別別の変動数量合計を取得
     * @param partCode 部品コード
     * @param transactionType 取引種別
     * @return 変動数量の合計
     */
    @Transactional(readOnly = true)
    public Long getTotalQuantityByTransactionType(String partCode, InventoryTransaction.TransactionType transactionType) {
        Long total = inventoryTransactionRepository.sumQuantityByPartCodeAndTransactionType(partCode, transactionType);
        return total != null ? total : 0L;
    }


}