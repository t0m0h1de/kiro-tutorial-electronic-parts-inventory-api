package com.example.inventory.repository;

import com.example.inventory.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 在庫変動履歴リポジトリインターフェース
 * Spring Data JPAを使用した在庫変動履歴の保存・検索機能を提供
 */
@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    /**
     * 部品コードで在庫変動履歴を検索（日時降順）
     * @param partCode 部品コード
     * @return 在庫変動履歴のリスト
     */
    List<InventoryTransaction> findByPartCodeOrderByTransactionDateDesc(String partCode);

    /**
     * 部品コードで在庫変動履歴を検索（ページネーション対応、日時降順）
     * @param partCode 部品コード
     * @param pageable ページネーション情報
     * @return 在庫変動履歴のPage
     */
    Page<InventoryTransaction> findByPartCodeOrderByTransactionDateDesc(String partCode, Pageable pageable);

    /**
     * 取引種別で在庫変動履歴を検索
     * @param transactionType 取引種別
     * @param pageable ページネーション情報
     * @return 在庫変動履歴のPage
     */
    Page<InventoryTransaction> findByTransactionTypeOrderByTransactionDateDesc(
            InventoryTransaction.TransactionType transactionType, Pageable pageable);

    /**
     * 期間指定で在庫変動履歴を検索
     * @param startDate 開始日時
     * @param endDate 終了日時
     * @param pageable ページネーション情報
     * @return 在庫変動履歴のPage
     */
    Page<InventoryTransaction> findByTransactionDateBetweenOrderByTransactionDateDesc(
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * 部品コードと期間指定で在庫変動履歴を検索
     * @param partCode 部品コード
     * @param startDate 開始日時
     * @param endDate 終了日時
     * @param pageable ページネーション情報
     * @return 在庫変動履歴のPage
     */
    Page<InventoryTransaction> findByPartCodeAndTransactionDateBetweenOrderByTransactionDateDesc(
            String partCode, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * 部品コードと取引種別で在庫変動履歴を検索
     * @param partCode 部品コード
     * @param transactionType 取引種別
     * @param pageable ページネーション情報
     * @return 在庫変動履歴のPage
     */
    Page<InventoryTransaction> findByPartCodeAndTransactionTypeOrderByTransactionDateDesc(
            String partCode, InventoryTransaction.TransactionType transactionType, Pageable pageable);

    /**
     * 最新の在庫変動履歴を取得（全体）
     * @param pageable ページネーション情報
     * @return 最新の在庫変動履歴のPage
     */
    Page<InventoryTransaction> findAllByOrderByTransactionDateDesc(Pageable pageable);

    /**
     * 部品コード別の在庫変動回数を取得
     * @return 部品コード別変動回数のリスト
     */
    @Query("SELECT i.partCode, COUNT(i) FROM InventoryTransaction i GROUP BY i.partCode")
    List<Object[]> countTransactionsByPartCode();

    /**
     * 期間内の在庫変動総数を取得
     * @param startDate 開始日時
     * @param endDate 終了日時
     * @return 在庫変動総数
     */
    @Query("SELECT COUNT(i) FROM InventoryTransaction i WHERE i.transactionDate BETWEEN :startDate AND :endDate")
    Long countTransactionsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * 部品コードの最新在庫変動履歴を取得
     * @param partCode 部品コード
     * @return 最新の在庫変動履歴（Optional）
     */
    @Query("SELECT i FROM InventoryTransaction i WHERE i.partCode = :partCode ORDER BY i.transactionDate DESC LIMIT 1")
    InventoryTransaction findLatestTransactionByPartCode(@Param("partCode") String partCode);

    /**
     * 取引種別別の変動数量合計を取得
     * @param partCode 部品コード
     * @param transactionType 取引種別
     * @return 変動数量の合計
     */
    @Query("SELECT SUM(i.quantity) FROM InventoryTransaction i WHERE i.partCode = :partCode AND i.transactionType = :transactionType")
    Long sumQuantityByPartCodeAndTransactionType(@Param("partCode") String partCode, 
                                                @Param("transactionType") InventoryTransaction.TransactionType transactionType);
}