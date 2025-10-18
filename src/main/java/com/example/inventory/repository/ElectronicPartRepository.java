package com.example.inventory.repository;

import com.example.inventory.entity.ElectronicPart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 電子部品リポジトリインターフェース
 * Spring Data JPAを使用したCRUD操作とカスタム検索メソッドを提供
 */
@Repository
public interface ElectronicPartRepository extends JpaRepository<ElectronicPart, Long> {

    /**
     * 部品コードで電子部品を検索
     * @param partCode 部品コード
     * @return 電子部品のOptional
     */
    Optional<ElectronicPart> findByPartCode(String partCode);

    /**
     * 部品コードで電子部品の存在確認
     * @param partCode 部品コード
     * @return 存在する場合true
     */
    boolean existsByPartCode(String partCode);

    /**
     * 部品名での部分一致検索（ページネーション対応）
     * @param partName 部品名（部分一致）
     * @param pageable ページネーション情報
     * @return 検索結果のPage
     */
    Page<ElectronicPart> findByPartNameContainingIgnoreCase(String partName, Pageable pageable);

    /**
     * カテゴリでの絞り込み検索（ページネーション対応）
     * @param category カテゴリ
     * @param pageable ページネーション情報
     * @return 検索結果のPage
     */
    Page<ElectronicPart> findByCategory(String category, Pageable pageable);

    /**
     * 在庫数量での範囲検索（ページネーション対応）
     * @param minStock 最小在庫数量
     * @param maxStock 最大在庫数量
     * @param pageable ページネーション情報
     * @return 検索結果のPage
     */
    Page<ElectronicPart> findByStockQuantityBetween(Integer minStock, Integer maxStock, Pageable pageable);

    /**
     * 複合検索メソッド - 部品名、カテゴリ、在庫数量での検索
     * @param partName 部品名（部分一致、nullの場合は条件に含めない）
     * @param category カテゴリ（nullの場合は条件に含めない）
     * @param minStock 最小在庫数量（nullの場合は条件に含めない）
     * @param maxStock 最大在庫数量（nullの場合は条件に含めない）
     * @param pageable ページネーション情報
     * @return 検索結果のPage
     */
    @Query("SELECT e FROM ElectronicPart e WHERE " +
           "(:partName IS NULL OR LOWER(e.partName) LIKE LOWER(CONCAT('%', :partName, '%'))) AND " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:minStock IS NULL OR e.stockQuantity >= :minStock) AND " +
           "(:maxStock IS NULL OR e.stockQuantity <= :maxStock)")
    Page<ElectronicPart> findBySearchCriteria(
            @Param("partName") String partName,
            @Param("category") String category,
            @Param("minStock") Integer minStock,
            @Param("maxStock") Integer maxStock,
            Pageable pageable);

    /**
     * 低在庫部品の検索（在庫数量が閾値以下の部品）
     * @param threshold 在庫閾値
     * @return 低在庫部品のリスト（在庫数量昇順）
     */
    @Query("SELECT e FROM ElectronicPart e WHERE e.stockQuantity <= :threshold ORDER BY e.stockQuantity ASC")
    List<ElectronicPart> findLowStockParts(@Param("threshold") Integer threshold);

    /**
     * 低在庫部品の検索（各部品の低在庫閾値を使用）
     * @return 低在庫部品のリスト（在庫数量昇順）
     */
    @Query("SELECT e FROM ElectronicPart e WHERE e.stockQuantity <= e.lowStockThreshold ORDER BY e.stockQuantity ASC")
    List<ElectronicPart> findLowStockPartsUsingThreshold();

    /**
     * カテゴリ別の部品数を取得
     * @return カテゴリ別部品数のリスト
     */
    @Query("SELECT e.category, COUNT(e) FROM ElectronicPart e GROUP BY e.category")
    List<Object[]> countPartsByCategory();

    /**
     * 在庫総数を取得
     * @return 全部品の在庫総数
     */
    @Query("SELECT SUM(e.stockQuantity) FROM ElectronicPart e")
    Long getTotalStockQuantity();

    /**
     * 部品コードで削除
     * @param partCode 部品コード
     */
    void deleteByPartCode(String partCode);
}