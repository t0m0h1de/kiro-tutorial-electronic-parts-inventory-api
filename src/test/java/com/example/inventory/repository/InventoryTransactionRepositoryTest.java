package com.example.inventory.repository;

import com.example.inventory.entity.ElectronicPart;
import com.example.inventory.entity.InventoryTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * InventoryTransactionRepositoryの統合テスト
 * H2データベースを使用したデータアクセステスト
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class InventoryTransactionRepositoryTest {

    @Autowired
    private InventoryTransactionRepository repository;

    @Autowired
    private ElectronicPartRepository partRepository;

    @Autowired
    private TestEntityManager entityManager;

    private ElectronicPart testPart1;
    private ElectronicPart testPart2;
    private InventoryTransaction transaction1;
    private InventoryTransaction transaction2;
    private InventoryTransaction transaction3;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        partRepository.deleteAll();
        
        // テスト用の電子部品を作成
        testPart1 = new ElectronicPart(
                "R001", "抵抗器 1kΩ", "抵抗", "1/4W カーボン抵抗",
                new BigDecimal("10.50"), 100, 20
        );
        
        testPart2 = new ElectronicPart(
                "C001", "コンデンサ 100μF", "コンデンサ", "電解コンデンサ 25V",
                new BigDecimal("25.00"), 50, 10
        );
        
        partRepository.saveAll(List.of(testPart1, testPart2));
        
        // テスト用の在庫変動履歴を作成
        LocalDateTime baseTime = LocalDateTime.now().minusDays(1);
        
        transaction1 = new InventoryTransaction(
                "R001", InventoryTransaction.TransactionType.INCREASE,
                50, 50, 100, "入荷"
        );
        transaction1.setTransactionDate(baseTime);
        
        transaction2 = new InventoryTransaction(
                "R001", InventoryTransaction.TransactionType.DECREASE,
                10, 100, 90, "出荷"
        );
        transaction2.setTransactionDate(baseTime.plusHours(1));
        
        transaction3 = new InventoryTransaction(
                "C001", InventoryTransaction.TransactionType.INCREASE,
                25, 25, 50, "入荷"
        );
        transaction3.setTransactionDate(baseTime.plusHours(2));
        
        repository.saveAll(List.of(transaction1, transaction2, transaction3));
        entityManager.flush();
    }

    @Test
    void findByPartCodeOrderByTransactionDateDesc_部品コードで検索_日時降順で取得できる() {
        // When
        List<InventoryTransaction> result = repository.findByPartCodeOrderByTransactionDateDesc("R001");
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTransactionType()).isEqualTo(InventoryTransaction.TransactionType.DECREASE);
        assertThat(result.get(1).getTransactionType()).isEqualTo(InventoryTransaction.TransactionType.INCREASE);
        // 日時が降順になっていることを確認
        assertThat(result.get(0).getTransactionDate()).isAfter(result.get(1).getTransactionDate());
    }

    @Test
    void findByPartCodeOrderByTransactionDateDesc_ページネーション_正しくページングされる() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);
        
        // When
        Page<InventoryTransaction> result = repository.findByPartCodeOrderByTransactionDateDesc("R001", pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2L);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
        // 最新の取引が最初に来ることを確認
        assertThat(result.getContent().get(0).getTransactionType())
                .isEqualTo(InventoryTransaction.TransactionType.DECREASE);
    }

    @Test
    void findByTransactionTypeOrderByTransactionDateDesc_取引種別で検索_該当する取引が取得できる() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<InventoryTransaction> result = repository.findByTransactionTypeOrderByTransactionDateDesc(
                InventoryTransaction.TransactionType.INCREASE, pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getPartCode()).isEqualTo("C001");
        assertThat(result.getContent().get(1).getPartCode()).isEqualTo("R001");
    }

    @Test
    void findByTransactionDateBetweenOrderByTransactionDateDesc_期間指定検索_該当する取引が取得できる() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<InventoryTransaction> result = repository.findByTransactionDateBetweenOrderByTransactionDateDesc(
                startDate, endDate, pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(3);
        // 日時降順で並んでいることを確認
        for (int i = 0; i < result.getContent().size() - 1; i++) {
            assertThat(result.getContent().get(i).getTransactionDate())
                    .isAfterOrEqualTo(result.getContent().get(i + 1).getTransactionDate());
        }
    }

    @Test
    void findByPartCodeAndTransactionDateBetweenOrderByTransactionDateDesc_部品コードと期間指定検索_該当する取引が取得できる() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<InventoryTransaction> result = repository.findByPartCodeAndTransactionDateBetweenOrderByTransactionDateDesc(
                "R001", startDate, endDate, pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getPartCode()).isEqualTo("R001");
        assertThat(result.getContent().get(1).getPartCode()).isEqualTo("R001");
    }

    @Test
    void findByPartCodeAndTransactionTypeOrderByTransactionDateDesc_部品コードと取引種別で検索_該当する取引が取得できる() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<InventoryTransaction> result = repository.findByPartCodeAndTransactionTypeOrderByTransactionDateDesc(
                "R001", InventoryTransaction.TransactionType.INCREASE, pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTransactionType())
                .isEqualTo(InventoryTransaction.TransactionType.INCREASE);
        assertThat(result.getContent().get(0).getQuantity()).isEqualTo(50);
    }

    @Test
    void findAllByOrderByTransactionDateDesc_全取引を日時降順で検索_正しく取得できる() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<InventoryTransaction> result = repository.findAllByOrderByTransactionDateDesc(pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(3);
        // 日時降順で並んでいることを確認
        for (int i = 0; i < result.getContent().size() - 1; i++) {
            assertThat(result.getContent().get(i).getTransactionDate())
                    .isAfterOrEqualTo(result.getContent().get(i + 1).getTransactionDate());
        }
    }

    @Test
    void countTransactionsByPartCode_部品コード別変動回数取得_正しい集計結果が取得できる() {
        // When
        List<Object[]> result = repository.countTransactionsByPartCode();
        
        // Then
        assertThat(result).hasSize(2);
        
        boolean foundR001 = false, foundC001 = false;
        for (Object[] row : result) {
            String partCode = (String) row[0];
            Long count = (Long) row[1];
            
            if ("R001".equals(partCode)) {
                assertThat(count).isEqualTo(2L);
                foundR001 = true;
            } else if ("C001".equals(partCode)) {
                assertThat(count).isEqualTo(1L);
                foundC001 = true;
            }
        }
        assertThat(foundR001 && foundC001).isTrue();
    }

    @Test
    void countTransactionsByDateRange_期間内変動総数取得_正しい件数が取得できる() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now();
        
        // When
        Long count = repository.countTransactionsByDateRange(startDate, endDate);
        
        // Then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    void findLatestTransactionByPartCode_最新取引履歴取得_正しい取引が取得できる() {
        // When
        InventoryTransaction result = repository.findLatestTransactionByPartCode("R001");
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionType()).isEqualTo(InventoryTransaction.TransactionType.DECREASE);
        assertThat(result.getQuantity()).isEqualTo(10);
    }

    @Test
    void sumQuantityByPartCodeAndTransactionType_取引種別別数量合計取得_正しい合計値が取得できる() {
        // When
        Long increaseSum = repository.sumQuantityByPartCodeAndTransactionType(
                "R001", InventoryTransaction.TransactionType.INCREASE);
        Long decreaseSum = repository.sumQuantityByPartCodeAndTransactionType(
                "R001", InventoryTransaction.TransactionType.DECREASE);
        
        // Then
        assertThat(increaseSum).isEqualTo(50L);
        assertThat(decreaseSum).isEqualTo(10L);
    }

    @Test
    void 存在しない部品コードで検索_空のリストが返される() {
        // When
        List<InventoryTransaction> result = repository.findByPartCodeOrderByTransactionDateDesc("NONEXISTENT");
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void 存在しない期間で検索_空のリストが返される() {
        // Given
        LocalDateTime futureStart = LocalDateTime.now().plusDays(1);
        LocalDateTime futureEnd = LocalDateTime.now().plusDays(2);
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<InventoryTransaction> result = repository.findByTransactionDateBetweenOrderByTransactionDateDesc(
                futureStart, futureEnd, pageable);
        
        // Then
        assertThat(result.getContent()).isEmpty();
    }
}