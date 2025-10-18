package com.example.inventory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 電子部品エンティティクラス
 * electronic_partsテーブルに対応するJPAエンティティ
 */
@Entity
@Table(name = "electronic_parts")
public class ElectronicPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "part_code", unique = true, nullable = false, length = 50)
    @NotBlank(message = "部品コードは必須です")
    @Size(max = 50, message = "部品コードは50文字以内で入力してください")
    @Pattern(regexp = "^[A-Za-z0-9\\-]+$", message = "部品コードは英数字とハイフンのみ使用可能です")
    private String partCode;

    @Column(name = "part_name", nullable = false, length = 200)
    @NotBlank(message = "部品名は必須です")
    @Size(max = 200, message = "部品名は200文字以内で入力してください")
    private String partName;

    @Column(name = "category", nullable = false, length = 100)
    @NotBlank(message = "カテゴリは必須です")
    @Size(max = 100, message = "カテゴリは100文字以内で入力してください")
    private String category;

    @Column(name = "specifications", columnDefinition = "TEXT")
    private String specifications;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "単価は必須です")
    @DecimalMin(value = "0.0", inclusive = true, message = "単価は0以上で入力してください")
    private BigDecimal unitPrice;

    @Column(name = "stock_quantity", nullable = false)
    @NotNull(message = "在庫数量は必須です")
    @Min(value = 0, message = "在庫数量は0以上で入力してください")
    private Integer stockQuantity;

    @Column(name = "low_stock_threshold", nullable = false)
    @NotNull(message = "低在庫閾値は必須です")
    @Min(value = 0, message = "低在庫閾値は0以上で入力してください")
    private Integer lowStockThreshold;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // デフォルトコンストラクタ
    public ElectronicPart() {
    }

    // コンストラクタ
    public ElectronicPart(String partCode, String partName, String category, 
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

    // JPA ライフサイクルメソッド
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ElectronicPart that = (ElectronicPart) o;
        return Objects.equals(partCode, that.partCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partCode);
    }

    // toString
    @Override
    public String toString() {
        return "ElectronicPart{" +
                "id=" + id +
                ", partCode='" + partCode + '\'' +
                ", partName='" + partName + '\'' +
                ", category='" + category + '\'' +
                ", unitPrice=" + unitPrice +
                ", stockQuantity=" + stockQuantity +
                ", lowStockThreshold=" + lowStockThreshold +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}