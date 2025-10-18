# 電子部品在庫管理API - OpenAPI/Swagger文書

## 概要

電子部品在庫管理APIのOpenAPI/Swagger文書が自動生成されるように設定されました。

## アクセス方法

### Swagger UI（推奨）
- URL: `http://localhost:8080/swagger-ui.html`
- ブラウザで直接アクセス可能
- インタラクティブなAPI文書
- 実際にAPIを試すことが可能

### OpenAPI JSON仕様書
- URL: `http://localhost:8080/v3/api-docs`
- JSON形式のOpenAPI 3.0仕様書
- 他のツールでのインポートに使用可能

### グループ別API文書
- URL: `http://localhost:8080/v3/api-docs/inventory-api`
- 在庫管理API専用のグループ化された仕様書

## 機能

### 自動生成される内容
- **API概要**: システムの説明、バージョン情報、連絡先
- **エンドポイント一覧**: 全てのREST APIエンドポイント
- **リクエスト/レスポンス例**: 実際のJSONサンプル
- **バリデーション規則**: 入力値の制約
- **エラーレスポンス**: 各種エラーケースの説明
- **データモデル**: DTOクラスのスキーマ定義

### 提供されるAPI群

#### 電子部品管理
- `POST /api/v1/parts` - 新規部品登録
- `GET /api/v1/parts/{partCode}` - 部品詳細取得
- `PUT /api/v1/parts/{partCode}` - 部品情報更新
- `DELETE /api/v1/parts/{partCode}` - 部品削除
- `GET /api/v1/parts` - 部品検索（ページネーション対応）

#### 在庫管理
- `PUT /api/v1/parts/{partCode}/inventory/increase` - 在庫増加
- `PUT /api/v1/parts/{partCode}/inventory/decrease` - 在庫減少
- `GET /api/v1/parts/low-stock` - 低在庫部品一覧

## 設定詳細

### application.yml設定
```yaml
springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    try-it-out-enabled: true
    operations-sorter: method
    tags-sorter: alpha
    display-request-duration: true
  show-actuator: true
  group-configs:
    - group: 'inventory-api'
      display-name: '電子部品在庫管理API'
      paths-to-match: '/api/v1/**'
```

### 使用されているアノテーション
- `@Tag`: コントローラのグループ化
- `@Operation`: エンドポイントの説明
- `@ApiResponses`: レスポンスパターンの定義
- `@Parameter`: パラメータの説明
- `@Schema`: データモデルの定義

## 使用方法

1. アプリケーションを起動
2. ブラウザで `http://localhost:8080/swagger-ui.html` にアクセス
3. APIエンドポイントを選択
4. 「Try it out」ボタンでAPIを実際に試行可能
5. リクエスト/レスポンスの例を確認

## 要件対応

この実装は以下の要件に対応しています：
- **要件 1.4**: 部品登録APIの仕様書
- **要件 2.3**: 部品更新APIの仕様書
- **要件 3.2**: 部品削除APIの仕様書
- **要件 4.4**: 部品検索APIの仕様書
- **要件 5.4**: 在庫調整APIの仕様書
- **要件 6.2**: 低在庫部品APIの仕様書