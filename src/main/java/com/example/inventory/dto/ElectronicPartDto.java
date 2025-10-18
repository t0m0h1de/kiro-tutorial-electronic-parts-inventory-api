package com.example.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 電子部品のDTO（Data Transfer Object）クラス
 * リクエスト/レスポンス用のデータ転送オブジェクト
 */
@Schema(description = "電子部品情報")
public class ElectronicPartDto {

    @Schema(description = "部品コード（一意識別子）", example = "R001", required = true)
    @NotBlank(message = "部品コードは必須です")
    @Size(max = 50, message = "部品コードは50文字以内で入力してください")
    @Pattern(regexp = "^[A-Za-z0-9\\-]+$", message = "部品コードは英数字とハイフンのみ使用可能です")
    private String partCode;

    @Schema(description = "部品名", example = "抵抗 1kΩ", required = true)
    @NotBlank(message = "部品名は必須です")
    @Size(max = 200, message = "部品名は200文字以内で入力してください")
    private String partName;

    @Schema(description = "部品カテゴリ", example = "抵抗", required = true)
    @NotBlank(message = "カテゴリは必須です")
    @Size(max = 100, message = "カテゴリは100文字以内で入力してください")
    private String category;

    @Schema(description = "部品仕様", example = "1/4W, ±5%")
    private String specifications;

    @Schema(description = "単価（円）", example = "10.50", required = true)
    @NotNull(message = "単価は必須です")
    @DecimalMin(value = "0.0", inclusive = true, message = "単価は0以上で入力してください")
    private BigDecimal unitPrice;

    @Schema(description = "現在の在庫数量", example = "85", required = true)
    @NotNull(message = "在庫数量は必須です")
    @Min(value = 0, message = "在庫数量は0以上で入力してください")
    private Integer stockQuantity;

    @Schema(description = "低在庫警告の閾値", example = "20", required = true)
    @NotNull(message = "低在庫閾値は必須です")
    @Min(value = 0, message = "低在庫閾値は0以上で入力してください")
    private Integer lowStockThreshold;

    @Schema(description = "作成日時", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新日時", example = "2024-01-01T15:30:00")
    private LocalDateTime updatedAt;

    // デフォルトコンストラクタ
    public ElectronicPartDto() {
    }

    // 全フィールドコンストラクタ
    public ElectronicPartDto(String partCode, String partName, String category, 
                           String specifications, BigDecimal unitPrice, 
                           Integer stockQuantity, Integer lowStockThreshold) {
        this.partCode = partCode;
        this.partName = partName;
        this.category = category;
        this.specifications = specifications;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
        this.lowStockThreshold = lowStockThreshold;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ElectronicPartDto{" +
                "partCode='" + partCode + '\'' +
                ", partName='" + partName + '\'' +
                ", category='" + category + '\'' +
                ", unitPrice=" + unitPrice +
                ", stockQuantity=" + stockQuantity +
                ", lowStockThreshold=" + lowStockThreshold +
                '}';
    }
}