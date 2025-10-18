package com.example.inventory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 在庫変動履歴エンティティクラス
 * inventory_transactionsテーブルに対応するJPAエンティティ
 */
@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "part_code", nullable = false, length = 50)
    @NotBlank(message = "部品コードは必須です")
    private String partCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_code", referencedColumnName = "part_code", insertable = false, updatable = false)
    private ElectronicPart electronicPart;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    @NotNull(message = "取引種別は必須です")
    private TransactionType transactionType;

    @Column(name = "quantity", nullable = false)
    @NotNull(message = "数量は必須です")
    private Integer quantity;

    @Column(name = "previous_stock", nullable = false)
    @NotNull(message = "変更前在庫は必須です")
    private Integer previousStock;

    @Column(name = "new_stock", nullable = false)
    @NotNull(message = "変更後在庫は必須です")
    private Integer newStock;

    @Column(name = "transaction_date", nullable = false)
    @NotNull(message = "取引日時は必須です")
    private LocalDateTime transactionDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // 取引種別の列挙型
    public enum TransactionType {
        INCREASE("増加"),
        DECREASE("減少");

        private final String displayName;

        TransactionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // デフォルトコンストラクタ
    public InventoryTransaction() {
    }

    // コンストラクタ
    public InventoryTransaction(String partCode, TransactionType transactionType, 
                              Integer quantity, Integer previousStock, 
                              Integer newStock, String notes) {
        this.partCode = partCode;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.previousStock = previousStock;
        this.newStock = newStock;
        this.notes = notes;
        this.transactionDate = LocalDateTime.now();
    }

    // JPA ライフサイクルメソッド
    @PrePersist
    protected void onCreate() {
        if (this.transactionDate == null) {
            this.transactionDate = LocalDateTime.now();
        }
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

    public ElectronicPart getElectronicPart() {
        return electronicPart;
    }

    public void setElectronicPart(ElectronicPart electronicPart) {
        this.electronicPart = electronicPart;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryTransaction that = (InventoryTransaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "InventoryTransaction{" +
                "id=" + id +
                ", partCode='" + partCode + '\'' +
                ", transactionType=" + transactionType +
                ", quantity=" + quantity +
                ", previousStock=" + previousStock +
                ", newStock=" + newStock +
                ", transactionDate=" + transactionDate +
                ", notes='" + notes + '\'' +
                '}';
    }
}