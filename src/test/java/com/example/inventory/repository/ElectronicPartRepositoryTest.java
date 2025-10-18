package com.example.inventory.repository;

import com.example.inventory.entity.ElectronicPart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ElectronicPartRepositoryの統合テスト
 * H2データベースを使用したデータアクセステスト
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ElectronicPartRepositoryTest {

    @Autowired
    private ElectronicPartRepository repository;

    private ElectronicPart testPart1;
    private ElectronicPart testPart2;
    private ElectronicPart testPart3;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        
        testPart1 = new ElectronicPart(
                "R001", "抵抗器 1kΩ", "抵抗", "1/4W カーボン抵抗",
                new BigDecimal("10.50"), 100, 20
        );
        
        testPart2 = new ElectronicPart(
                "C001", "コンデンサ 100μF", "コンデンサ", "電解コンデンサ 25V",
                new BigDecimal("25.00"), 50, 10
        );
        
        testPart3 = new ElectronicPart(
                "IC001", "マイコン ATmega328P", "IC", "8ビット AVRマイコン",
                new BigDecimal("350.00"), 5, 10
        );
        
        repository.saveAll(List.of(testPart1, testPart2, testPart3));
    }

    @Test
    void findByPartCode_存在する部品コードで検索_部品が取得できる() {
        // When
        Optional<ElectronicPart> result = repository.findByPartCode("R001");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPartName()).isEqualTo("抵抗器 1kΩ");
        assertThat(result.get().getCategory()).isEqualTo("抵抗");
    }

    @Test
    void findByPartCode_存在しない部品コードで検索_空のOptionalが返される() {
        // When
        Optional<ElectronicPart> result = repository.findByPartCode("NONEXISTENT");
        
        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void existsByPartCode_存在する部品コード_trueが返される() {
        // When
        boolean exists = repository.existsByPartCode("R001");
        
        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByPartCode_存在しない部品コード_falseが返される() {
        // When
        boolean exists = repository.existsByPartCode("NONEXISTENT");
        
        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByPartNameContainingIgnoreCase_部分一致検索_該当する部品が取得できる() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<ElectronicPart> result = repository.findByPartNameContainingIgnoreCase("抵抗", pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPartCode()).isEqualTo("R001");
    }

    @Test
    void findByPartNameContainingIgnoreCase_大文字小文字を無視した検索_該当する部品が取得できる() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<ElectronicPart> result = repository.findByPartNameContainingIgnoreCase("ATMEGA", pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPartCode()).isEqualTo("IC001");
    }

    @Test
    void findByCategory_カテゴリ検索_該当する部品が取得できる() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<ElectronicPart> result = repository.findByCategory("コンデンサ", pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPartCode()).isEqualTo("C001");
    }

    @Test
    void findByStockQuantityBetween_在庫数量範囲検索_該当する部品が取得できる() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<ElectronicPart> result = repository.findByStockQuantityBetween(40, 60, pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPartCode()).isEqualTo("C001");
        assertThat(result.getContent().get(0).getStockQuantity()).isEqualTo(50);
    }

    @Test
    void findBySearchCriteria_複合検索_全条件指定_該当する部品が取得できる() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<ElectronicPart> result = repository.findBySearchCriteria(
                "抵抗", "抵抗", 50, 150, pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPartCode()).isEqualTo("R001");
    }

    @Test
    void findBySearchCriteria_複合検索_部品名のみ指定_該当する部品が取得できる() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<ElectronicPart> result = repository.findBySearchCriteria(
                "コンデンサ", null, null, null, pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPartCode()).isEqualTo("C001");
    }

    @Test
    void findBySearchCriteria_複合検索_条件に一致しない_空のリストが返される() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        // When
        Page<ElectronicPart> result = repository.findBySearchCriteria(
                "存在しない部品", null, null, null, pageable);
        
        // Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findLowStockParts_指定閾値以下の部品検索_在庫数量昇順で取得できる() {
        // When
        List<ElectronicPart> result = repository.findLowStockParts(15);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPartCode()).isEqualTo("IC001");
        assertThat(result.get(0).getStockQuantity()).isEqualTo(5);
    }

    @Test
    void findLowStockPartsUsingThreshold_各部品の閾値を使用した低在庫検索_該当する部品が取得できる() {
        // When
        List<ElectronicPart> result = repository.findLowStockPartsUsingThreshold();
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPartCode()).isEqualTo("IC001");
        assertThat(result.get(0).getStockQuantity()).isEqualTo(5);
        assertThat(result.get(0).getLowStockThreshold()).isEqualTo(10);
    }

    @Test
    void countPartsByCategory_カテゴリ別部品数取得_正しい集計結果が取得できる() {
        // When
        List<Object[]> result = repository.countPartsByCategory();
        
        // Then
        assertThat(result).hasSize(3);
        // 結果の検証（順序は保証されないため、カテゴリ名で検索）
        boolean foundResistor = false, foundCapacitor = false, foundIC = false;
        for (Object[] row : result) {
            String category = (String) row[0];
            Long count = (Long) row[1];
            
            if ("抵抗".equals(category)) {
                assertThat(count).isEqualTo(1L);
                foundResistor = true;
            } else if ("コンデンサ".equals(category)) {
                assertThat(count).isEqualTo(1L);
                foundCapacitor = true;
            } else if ("IC".equals(category)) {
                assertThat(count).isEqualTo(1L);
                foundIC = true;
            }
        }
        assertThat(foundResistor && foundCapacitor && foundIC).isTrue();
    }

    @Test
    void getTotalStockQuantity_在庫総数取得_正しい合計値が取得できる() {
        // When
        Long totalStock = repository.getTotalStockQuantity();
        
        // Then
        assertThat(totalStock).isEqualTo(155L); // 100 + 50 + 5
    }

    @Test
    void deleteByPartCode_部品コードで削除_該当する部品が削除される() {
        // Given
        assertThat(repository.existsByPartCode("R001")).isTrue();
        
        // When
        repository.deleteByPartCode("R001");
        
        // Then
        assertThat(repository.existsByPartCode("R001")).isFalse();
        assertThat(repository.count()).isEqualTo(2L);
    }

    @Test
    void ページネーション機能_正しくページングされる() {
        // Given
        Pageable firstPage = PageRequest.of(0, 2);
        Pageable secondPage = PageRequest.of(1, 2);
        
        // When
        Page<ElectronicPart> firstResult = repository.findAll(firstPage);
        Page<ElectronicPart> secondResult = repository.findAll(secondPage);
        
        // Then
        assertThat(firstResult.getContent()).hasSize(2);
        assertThat(firstResult.getTotalElements()).isEqualTo(3L);
        assertThat(firstResult.getTotalPages()).isEqualTo(2);
        assertThat(firstResult.hasNext()).isTrue();
        
        assertThat(secondResult.getContent()).hasSize(1);
        assertThat(secondResult.hasNext()).isFalse();
    }
}