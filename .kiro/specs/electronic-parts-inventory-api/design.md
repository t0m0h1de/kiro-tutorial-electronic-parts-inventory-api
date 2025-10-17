# 設計書

## 概要

電子部品製品の在庫管理APIは、Java Spring BootフレームワークとMariaDBデータベースを使用したREST APIシステムです。レイヤードアーキテクチャを採用し、Controller、Service、Repository層に分離して実装します。

## アーキテクチャ

### システム構成

```
┌─────────────────┐
│   REST Client   │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│  Controller層   │ ← REST APIエンドポイント
└─────────────────┘
         │
         ▼
┌─────────────────┐
│   Service層     │ ← ビジネスロジック
└─────────────────┘
         │
         ▼
┌─────────────────┐
│  Repository層   │ ← データアクセス
└─────────────────┘
         │
         ▼
┌─────────────────┐
│   MariaDB       │ ← データベース
└─────────────────┘
```

### 技術スタック

- **フレームワーク**: Spring Boot 3.x
- **データベース**: MariaDB 10.x
- **ORM**: Spring Data JPA
- **バリデーション**: Spring Boot Validation
- **ドキュメント**: OpenAPI 3.0 (Swagger)
- **ビルドツール**: Maven
- **Java**: Java 17+

## コンポーネントとインターfaces

### REST APIエンドポイント

#### 電子部品管理

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| POST | `/api/v1/parts` | 新規部品登録 |
| GET | `/api/v1/parts/{partCode}` | 部品詳細取得 |
| PUT | `/api/v1/parts/{partCode}` | 部品情報更新 |
| DELETE | `/api/v1/parts/{partCode}` | 部品削除 |
| GET | `/api/v1/parts` | 部品検索（ページネーション対応） |

#### 在庫管理

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| PUT | `/api/v1/parts/{partCode}/inventory/increase` | 在庫増加 |
| PUT | `/api/v1/parts/{partCode}/inventory/decrease` | 在庫減少 |
| GET | `/api/v1/parts/low-stock` | 低在庫部品一覧 |

### コンポーネント構成

#### Controller層
- `ElectronicPartController`: 電子部品のCRUD操作
- `InventoryController`: 在庫管理操作
- `GlobalExceptionHandler`: 統一エラーハンドリング

#### Service層
- `ElectronicPartService`: 電子部品ビジネスロジック
- `InventoryService`: 在庫管理ビジネスロジック

#### Repository層
- `ElectronicPartRepository`: 電子部品データアクセス
- `InventoryTransactionRepository`: 在庫変動履歴データアクセス

## データモデル

### データベース設計について

データベースのテーブル設計とDDL作成は別チームが担当します。以下は想定されるテーブル構造の概要です：

### 電子部品テーブル (electronic_parts) - 想定構造

- **主要フィールド**: 
  - id (主キー)
  - part_code (部品コード、ユニーク)
  - part_name (部品名)
  - category (カテゴリ)
  - specifications (仕様)
  - unit_price (単価)
  - stock_quantity (在庫数量)
  - low_stock_threshold (低在庫閾値)
  - created_at, updated_at (タイムスタンプ)

### 在庫変動履歴テーブル (inventory_transactions) - 想定構造

- **主要フィールド**:
  - id (主キー)
  - part_code (部品コード、外部キー)
  - transaction_type (取引種別: INCREASE/DECREASE)
  - quantity (数量)
  - previous_stock (変更前在庫)
  - new_stock (変更後在庫)
  - transaction_date (取引日時)
  - notes (備考)

### DTOクラス

#### ElectronicPartDto
```java
public class ElectronicPartDto {
    private String partCode;
    private String partName;
    private String category;
    private String specifications;
    private BigDecimal unitPrice;
    private Integer stockQuantity;
    private Integer lowStockThreshold;
}
```

#### InventoryAdjustmentDto
```java
public class InventoryAdjustmentDto {
    private String partCode;
    private Integer quantity;
    private String notes;
}
```

#### SearchCriteriaDto
```java
public class SearchCriteriaDto {
    private String partName;
    private String category;
    private Integer minStock;
    private Integer maxStock;
    private Integer page;
    private Integer size;
}
```

## エラーハンドリング

### 統一エラーレスポンス形式

```json
{
    "timestamp": "2024-01-01T10:00:00Z",
    "status": 400,
    "error": "Bad Request",
    "message": "部品コードは必須です",
    "path": "/api/v1/parts"
}
```

### エラー種別

| HTTPステータス | エラー種別 | 説明 |
|---------------|-----------|------|
| 400 | Bad Request | リクエスト形式エラー、バリデーションエラー |
| 404 | Not Found | 指定された部品が存在しない |
| 409 | Conflict | 部品コード重複エラー |
| 500 | Internal Server Error | システムエラー、DB接続エラー |

### バリデーション規則

- **部品コード**: 必須、1-50文字、英数字とハイフンのみ
- **部品名**: 必須、1-200文字
- **カテゴリ**: 必須、1-100文字
- **単価**: 必須、0以上の数値
- **在庫数量**: 必須、0以上の整数
- **低在庫閾値**: 必須、0以上の整数

## テスト戦略

### 単体テスト
- **Controller層**: MockMvcを使用したAPIエンドポイントテスト
- **Service層**: ビジネスロジックの単体テスト
- **Repository層**: @DataJpaTestを使用したデータアクセステスト

### 統合テスト
- **API統合テスト**: @SpringBootTestを使用したエンドツーエンドテスト
- **データベース統合テスト**: TestContainersを使用したMariaDBテスト

### テストデータ
- **テスト用データベース**: MariaDBコンテナ（単体テスト用）
- **統合テスト用データベース**: TestContainers MariaDB

## セキュリティ考慮事項

### 入力検証
- すべての入力パラメータに対するバリデーション
- SQLインジェクション対策（JPA使用）
- XSS対策（入力値のサニタイズ）

### ログ出力
- アクセスログ（リクエスト/レスポンス）
- エラーログ（例外情報）
- 在庫変動ログ（監査証跡）

## パフォーマンス考慮事項

### データベース最適化
- 適切なインデックス設定
- ページネーション実装
- N+1問題対策（JPA Fetch戦略）

### キャッシュ戦略
- Spring Cacheを使用した部品情報キャッシュ
- 低在庫部品リストのキャッシュ（TTL: 5分）

## 設定管理

### application.yml構成例

```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/inventory_db
    username: ${DB_USERNAME:inventory_user}
    password: ${DB_PASSWORD:password}
    driver-class-name: org.mariadb.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MariaDBDialect
        format_sql: true
  
  cache:
    type: simple

logging:
  level:
    com.example.inventory: INFO
    org.springframework.web: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```