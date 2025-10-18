# Electronic Parts Inventory API

電子部品在庫管理システムのREST APIです。

## 必要な環境

- Java 17以上
- Maven 3.6以上
- MariaDB 10.x

## プロジェクト構造

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           └── inventory/
│   │               ├── ElectronicPartsInventoryApiApplication.java
│   │               ├── controller/     # REST APIエンドポイント
│   │               ├── service/        # ビジネスロジック
│   │               ├── repository/     # データアクセス
│   │               ├── dto/           # データ転送オブジェクト
│   │               └── entity/        # JPAエンティティ
│   └── resources/
│       └── application.yml            # アプリケーション設定
└── test/
    ├── java/
    │   └── com/
    │       └── example/
    │           └── inventory/
    └── resources/
        └── application-test.yml       # テスト用設定
```

## ビルドと実行

```bash
# プロジェクトのビルド
./mvnw clean compile

# テストの実行
./mvnw test

# アプリケーションの起動
./mvnw spring-boot:run
```

## API文書

アプリケーション起動後、以下のURLでSwagger UIにアクセスできます：
- http://localhost:8080/swagger-ui.html

## データベース設定

application.ymlでMariaDBの接続設定を行います。
環境変数でデータベースの認証情報を設定できます：

- `DB_USERNAME`: データベースユーザー名（デフォルト: inventory_user）
- `DB_PASSWORD`: データベースパスワード（デフォルト: password）