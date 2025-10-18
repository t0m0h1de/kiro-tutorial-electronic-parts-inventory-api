package com.example.inventory.dto;

import com.example.inventory.entity.ElectronicPart;
import com.example.inventory.entity.InventoryTransaction;
import org.springframework.stereotype.Component;

/**
 * 在庫調整レスポンス用のマッピングクラス
 * ElectronicPartとInventoryTransactionからInventoryAdjustmentResponseDtoへの変換を行う
 */
@Component
public class InventoryAdjustmentResponseMapper {

    /**
     * ElectronicPartとInventoryTransactionからInventoryAdjustmentResponseDtoを作成
     * @param part 電子部品エンティティ
     * @param transaction 在庫変動履歴エンティティ
     * @return 在庫調整レスポンスDTO
     */
    public InventoryAdjustmentResponseDto toResponseDto(ElectronicPart part, InventoryTransaction transaction) {
        if (part == null || transaction == null) {
            return null;
        }

        InventoryAdjustmentResponseDto responseDto = new InventoryAdjustmentResponseDto();
        responseDto.setPartCode(part.getPartCode());
        responseDto.setPartName(part.getPartName());
        responseDto.setPreviousStock(transaction.getPreviousStock());
        responseDto.setNewStock(transaction.getNewStock());
        responseDto.setAdjustmentQuantity(transaction.getQuantity());
        responseDto.setAdjustmentType(transaction.getTransactionType().getDisplayName());
        responseDto.setAdjustmentDate(transaction.getTransactionDate());
        responseDto.setNotes(transaction.getNotes());

        return responseDto;
    }

    /**
     * 在庫調整結果からInventoryAdjustmentResponseDtoを作成（簡易版）
     * @param partCode 部品コード
     * @param partName 部品名
     * @param previousStock 変更前在庫
     * @param newStock 変更後在庫
     * @param adjustmentQuantity 調整数量
     * @param adjustmentType 調整種別
     * @param notes 備考
     * @return 在庫調整レスポンスDTO
     */
    public InventoryAdjustmentResponseDto createResponseDto(String partCode, String partName,
                                                          Integer previousStock, Integer newStock,
                                                          Integer adjustmentQuantity, String adjustmentType,
                                                          String notes) {
        InventoryAdjustmentResponseDto responseDto = new InventoryAdjustmentResponseDto();
        responseDto.setPartCode(partCode);
        responseDto.setPartName(partName);
        responseDto.setPreviousStock(previousStock);
        responseDto.setNewStock(newStock);
        responseDto.setAdjustmentQuantity(adjustmentQuantity);
        responseDto.setAdjustmentType(adjustmentType);
        responseDto.setAdjustmentDate(java.time.LocalDateTime.now());
        responseDto.setNotes(notes);

        return responseDto;
    }
}