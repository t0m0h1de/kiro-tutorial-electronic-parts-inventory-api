package com.example.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * 在庫調整のDTO（Data Transfer Object）クラス
 * 在庫増減リクエスト用のデータ転送オブジェクト
 */
@Schema(description = "在庫調整リクエスト情報")
public class InventoryAdjustmentDto {

    @Schema(description = "部品コード（パスパラメータから自動設定）", example = "R001")
    @NotBlank(message = "部品コードは必須です")
    @Size(max = 50, message = "部品コードは50文字以内で入力してください")
    @Pattern(regexp = "^[A-Za-z0-9\\-]+$", message = "部品コードは英数字とハイフンのみ使用可能です")
    private String partCode;

    @Schema(description = "調整数量", example = "50", required = true)
    @NotNull(message = "数量は必須です")
    @Min(value = 1, message = "数量は1以上で入力してください")
    private Integer quantity;

    @Schema(description = "調整理由や備考", example = "新規入荷")
    @Size(max = 500, message = "備考は500文字以内で入力してください")
    private String notes;

    // デフォルトコンストラクタ
    public InventoryAdjustmentDto() {
    }

    // コンストラクタ
    public InventoryAdjustmentDto(String partCode, Integer quantity, String notes) {
        this.partCode = partCode;
        this.quantity = quantity;
        this.notes = notes;
    }

    // Getter and Setter methods
    public String getPartCode() {
        return partCode;
    }

    public void setPartCode(String partCode) {
        this.partCode = partCode;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "InventoryAdjustmentDto{" +
                "partCode='" + partCode + '\'' +
                ", quantity=" + quantity +
                ", notes='" + notes + '\'' +
                '}';
    }
}