package com.example.inventory.dto;

import jakarta.validation.constraints.*;

/**
 * 検索条件のDTO（Data Transfer Object）クラス
 * 部品検索リクエスト用のデータ転送オブジェクト
 */
public class SearchCriteriaDto {

    @Size(max = 200, message = "部品名は200文字以内で入力してください")
    private String partName;

    @Size(max = 100, message = "カテゴリは100文字以内で入力してください")
    private String category;

    @Min(value = 0, message = "最小在庫数は0以上で入力してください")
    private Integer minStock;

    @Min(value = 0, message = "最大在庫数は0以上で入力してください")
    private Integer maxStock;

    @Min(value = 0, message = "ページ番号は0以上で入力してください")
    private Integer page = 0;

    @Min(value = 1, message = "ページサイズは1以上で入力してください")
    @Max(value = 100, message = "ページサイズは100以下で入力してください")
    private Integer size = 20;

    // デフォルトコンストラクタ
    public SearchCriteriaDto() {
    }

    // コンストラクタ
    public SearchCriteriaDto(String partName, String category, Integer minStock, 
                           Integer maxStock, Integer page, Integer size) {
        this.partName = partName;
        this.category = category;
        this.minStock = minStock;
        this.maxStock = maxStock;
        this.page = page != null ? page : 0;
        this.size = size != null ? size : 20;
    }

    // Getter and Setter methods
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

    public Integer getMinStock() {
        return minStock;
    }

    public void setMinStock(Integer minStock) {
        this.minStock = minStock;
    }

    public Integer getMaxStock() {
        return maxStock;
    }

    public void setMaxStock(Integer maxStock) {
        this.maxStock = maxStock;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page != null ? page : 0;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size != null ? size : 20;
    }

    /**
     * 在庫数量の範囲検証
     * @return 最小在庫数が最大在庫数以下の場合true
     */
    public boolean isValidStockRange() {
        if (minStock != null && maxStock != null) {
            return minStock <= maxStock;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SearchCriteriaDto{" +
                "partName='" + partName + '\'' +
                ", category='" + category + '\'' +
                ", minStock=" + minStock +
                ", maxStock=" + maxStock +
                ", page=" + page +
                ", size=" + size +
                '}';
    }
}