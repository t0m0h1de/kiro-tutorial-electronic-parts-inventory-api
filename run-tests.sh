#!/bin/bash

# MariaDBコンテナを起動してテストを実行するスクリプト

set -e

echo "🚀 MariaDBコンテナを起動中..."

# 既存のコンテナを停止・削除
podman compose -f container-compose.yaml down --volumes 2>/dev/null || true

# コンテナを起動
podman compose -f container-compose.yaml up -d

echo "⏳ MariaDBの起動を待機中..."

# MariaDBが起動するまで待機
max_attempts=30
attempt=0

while [ $attempt -lt $max_attempts ]; do
    if podman exec inventory-mariadb-test mysqladmin ping -h localhost -u test -ptest --silent 2>/dev/null; then
        echo "✅ MariaDBが起動しました"
        break
    fi
    
    attempt=$((attempt + 1))
    echo "待機中... ($attempt/$max_attempts)"
    sleep 2
done

if [ $attempt -eq $max_attempts ]; then
    echo "❌ MariaDBの起動がタイムアウトしました"
    podman compose -f container-compose.yaml logs mariadb-test
    exit 1
fi

echo "🧪 統合テストを実行中..."

# テストを実行
mvn test -Dtest=ElectronicPartRepositoryTest,InventoryTransactionRepositoryTest

test_result=$?

echo "🧹 コンテナをクリーンアップ中..."

# コンテナを停止・削除
podman compose -f container-compose.yaml down --volumes

if [ $test_result -eq 0 ]; then
    echo "✅ すべてのテストが成功しました！"
else
    echo "❌ テストが失敗しました"
    exit 1
fi