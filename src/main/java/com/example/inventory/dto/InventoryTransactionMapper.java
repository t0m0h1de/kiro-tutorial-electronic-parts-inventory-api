package com.example.inventory.dto;

import com.example.inventory.entity.InventoryTransaction;
import org.springframework.stereotype.Component;

/**
 * InventoryTransactionエンティティとInventoryAdjustmentDto間のマッピングクラス
 * 在庫調整DTOから在庫変動履歴エンティティへの変換を行う
 */
@Component
public class InventoryTransactionMapper {

    /**
     * InventoryAdjustmentDtoからInventoryTransactionエンティティを作成
     * @param dto 在庫調整DTO
     * @param transactionType 取引種別（INCREASE/DECREASE）
     * @param previousStock 変更前在庫数量
     * @param newStock 変更後在庫数量
     * @return 作成されたInventoryTransactionエンティティ
     */
    public InventoryTransaction createTransaction(InventoryAdjustmentDto dto, 
                                                InventoryTransaction.TransactionType transactionType,
                                                Integer previousStock, 
                                                Integer newStock) {
        if (dto == null) {
            return null;
        }

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setPartCode(dto.getPartCode());
        transaction.setTransactionType(transactionType);
        transaction.setQuantity(dto.getQuantity());
        transaction.setPreviousStock(previousStock);
        transaction.setNewStock(newStock);
        transaction.setNotes(dto.getNotes());
        // transactionDateはJPAライフサイクルメソッドで自動設定

        return transaction;
    }

    /**
     * 在庫増加用のInventoryTransactionエンティティを作成
     * @param dto 在庫調整DTO
     * @param previousStock 変更前在庫数量
     * @param newStock 変更後在庫数量
     * @return 作成されたInventoryTransactionエンティティ
     */
    public InventoryTransaction createIncreaseTransaction(InventoryAdjustmentDto dto, 
                                                        Integer previousStock, 
                                                        Integer newStock) {
        return createTransaction(dto, InventoryTransaction.TransactionType.INCREASE, 
                               previousStock, newStock);
    }

    /**
     * 在庫減少用のInventoryTransactionエンティティを作成
     * @param dto 在庫調整DTO
     * @param previousStock 変更前在庫数量
     * @param newStock 変更後在庫数量
     * @return 作成されたInventoryTransactionエンティティ
     */
    public InventoryTransaction createDecreaseTransaction(InventoryAdjustmentDto dto, 
                                                        Integer previousStock, 
                                                        Integer newStock) {
        return createTransaction(dto, InventoryTransaction.TransactionType.DECREASE, 
                               previousStock, newStock);
    }
}