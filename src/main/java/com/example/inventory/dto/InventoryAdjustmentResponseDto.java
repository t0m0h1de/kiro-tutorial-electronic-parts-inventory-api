package com.example.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 在庫調整レスポンス用のDTO（Data Transfer Object）クラス
 * 在庫調整操作の結果を返すためのデータ転送オブジェクト
 */
@Schema(description = "在庫調整結果情報")
public class InventoryAdjustmentResponseDto {

    @Schema(description = "部品コード", example = "R001")
    private String partCode;
    
    @Schema(description = "部品名", example = "抵抗 1kΩ")
    private String partName;
    
    @Schema(description = "調整前在庫数量", example = "85")
    private Integer previousStock;
    
    @Schema(description = "調整後在庫数量", example = "135")
    private Integer newStock;
    
    @Schema(description = "調整数量", example = "50")
    private Integer adjustmentQuantity;
    
    @Schema(description = "調整種別", example = "INCREASE", allowableValues = {"INCREASE", "DECREASE"})
    private String adjustmentType;
    
    @Schema(description = "調整実行日時", example = "2024-01-01T16:30:00")
    private LocalDateTime adjustmentDate;
    
    @Schema(description = "調整理由や備考", example = "新規入荷")
    private String notes;

    // デフォルトコンストラクタ
    public InventoryAdjustmentResponseDto() {
    }

    // コンストラクタ
    public InventoryAdjustmentResponseDto(String partCode, String partName, 
                                        Integer previousStock, Integer newStock, 
                                        Integer adjustmentQuantity, String adjustmentType,
                                        LocalDateTime adjustmentDate, String notes) {
        this.partCode = partCode;
        this.partName = partName;
        this.previousStock = previousStock;
        this.newStock = newStock;
        this.adjustmentQuantity = adjustmentQuantity;
        this.adjustmentType = adjustmentType;
        this.adjustmentDate = adjustmentDate;
        this.notes = notes;
    }

    // Getter and Setter methods
    public String getPartCode() {
        return partCode;
    }

    public void setPartCode(String partCode) {
        this.partCode = partCode;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public Integer getPreviousStock() {
        return previousStock;
    }

    public void setPreviousStock(Integer previousStock) {
        this.previousStock = previousStock;
    }

    public Integer getNewStock() {
        return newStock;
    }

    public void setNewStock(Integer newStock) {
        this.newStock = newStock;
    }

    public Integer getAdjustmentQuantity() {
        return adjustmentQuantity;
    }

    public void setAdjustmentQuantity(Integer adjustmentQuantity) {
        this.adjustmentQuantity = adjustmentQuantity;
    }

    public String getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(String adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    public LocalDateTime getAdjustmentDate() {
        return adjustmentDate;
    }

    public void setAdjustmentDate(LocalDateTime adjustmentDate) {
        this.adjustmentDate = adjustmentDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "InventoryAdjustmentResponseDto{" +
                "partCode='" + partCode + '\'' +
                ", partName='" + partName + '\'' +
                ", previousStock=" + previousStock +
                ", newStock=" + newStock +
                ", adjustmentQuantity=" + adjustmentQuantity +
                ", adjustmentType='" + adjustmentType + '\'' +
                ", adjustmentDate=" + adjustmentDate +
                ", notes='" + notes + '\'' +
                '}';
    }
}