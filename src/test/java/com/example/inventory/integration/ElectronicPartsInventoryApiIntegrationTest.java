package com.example.inventory.integration;

import com.example.inventory.dto.ElectronicPartDto;
import com.example.inventory.dto.InventoryAdjustmentDto;
import com.example.inventory.dto.SearchCriteriaDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebMvc
@Transactional
class ElectronicPartsInventoryApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldCreateElectronicPart() throws Exception {
        // 要件 1.1, 1.4: 新規部品登録
        ElectronicPartDto partDto = createTestPartDto("TEST-001", "テスト抵抗", "抵抗");

        mockMvc.perform(post("/api/v1/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.partCode").value("TEST-001"))
                .andExpect(jsonPath("$.partName").value("テスト抵抗"))
                .andExpect(jsonPath("$.category").value("抵抗"))
                .andExpect(jsonPath("$.stockQuantity").value(100));
    }

    @Test
    void shouldReturnConflictWhenDuplicatePartCode() throws Exception {
        // 要件 1.2: 部品コード重複エラー
        ElectronicPartDto partDto = createTestPartDto("DUP-001", "重複テスト", "抵抗");

        // 最初の登録
        mockMvc.perform(post("/api/v1/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partDto)))
                .andExpect(status().isCreated());

        // 重複登録
        mockMvc.perform(post("/api/v1/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("部品コード")));
    }

    @Test
    void shouldReturnBadRequestWhenValidationFails() throws Exception {
        // 要件 1.3: バリデーションエラー
        ElectronicPartDto invalidPartDto = new ElectronicPartDto();
        // 必須項目を設定しない

        mockMvc.perform(post("/api/v1/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPartDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetElectronicPartByCode() throws Exception {
        // 要件 2.1: 部品詳細取得
        ElectronicPartDto partDto = createTestPartDto("GET-001", "取得テスト", "コンデンサ");
        
        // 部品を登録
        mockMvc.perform(post("/api/v1/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partDto)))
                .andExpect(status().isCreated());

        // 部品を取得
        mockMvc.perform(get("/api/v1/parts/GET-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partCode").value("GET-001"))
                .andExpect(jsonPath("$.partName").value("取得テスト"))
                .andExpect(jsonPath("$.category").value("コンデンサ"));
    }

    @Test
    void shouldReturnNotFoundWhenPartDoesNotExist() throws Exception {
        // 要件 2.2, 3.2: 存在しない部品コードでの404エラー
        mockMvc.perform(get("/api/v1/parts/NONEXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("部品が見つかりません")));
    }

    @Test
    void shouldUpdateElectronicPart() throws Exception {
        // 要件 2.1, 2.3: 部品情報更新
        ElectronicPartDto originalPart = createTestPartDto("UPD-001", "更新前", "抵抗");
        
        // 部品を登録
        mockMvc.perform(post("/api/v1/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(originalPart)))
                .andExpect(status().isCreated());

        // 部品を更新
        ElectronicPartDto updatedPart = createTestPartDto("UPD-001", "更新後", "コンデンサ");
        updatedPart.setUnitPrice(new BigDecimal("200.00"));

        mockMvc.perform(put("/api/v1/parts/UPD-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedPart)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partName").value("更新後"))
                .andExpect(jsonPath("$.category").value("コンデンサ"))
                .andExpect(jsonPath("$.unitPrice").value(200.00));
    }

    @Test
    void shouldDeleteElectronicPart() throws Exception {
        // 要件 3.1, 3.3: 部品削除
        ElectronicPartDto partDto = createTestPartDto("DEL-001", "削除テスト", "IC");
        
        // 部品を登録
        mockMvc.perform(post("/api/v1/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partDto)))
                .andExpect(status().isCreated());

        // 部品を削除
        mockMvc.perform(delete("/api/v1/parts/DEL-001"))
                .andExpect(status().isNoContent());

        // 削除確認
        mockMvc.perform(get("/api/v1/parts/DEL-001"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldSearchPartsByName() throws Exception {
        // 要件 4.1: 部品名での部分一致検索
        createAndRegisterPart("SEARCH-001", "検索テスト抵抗", "抵抗");
        createAndRegisterPart("SEARCH-002", "検索テストコンデンサ", "コンデンサ");
        createAndRegisterPart("SEARCH-003", "別の部品", "IC");

        mockMvc.perform(get("/api/v1/parts")
                        .param("partName", "検索テスト")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].partName", everyItem(containsString("検索テスト"))));
    }

    @Test
    void shouldSearchPartsByCategory() throws Exception {
        // 要件 4.2: カテゴリでの絞り込み検索
        createAndRegisterPart("CAT-001", "抵抗1", "抵抗");
        createAndRegisterPart("CAT-002", "抵抗2", "抵抗");
        createAndRegisterPart("CAT-003", "コンデンサ1", "コンデンサ");

        mockMvc.perform(get("/api/v1/parts")
                        .param("category", "抵抗")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].category", everyItem(is("抵抗"))));
    }

    @Test
    void shouldSearchPartsByStockRange() throws Exception {
        // 要件 4.3: 在庫数量での範囲検索
        createAndRegisterPartWithStock("STOCK-001", "低在庫", "抵抗", 5);
        createAndRegisterPartWithStock("STOCK-002", "中在庫", "抵抗", 50);
        createAndRegisterPartWithStock("STOCK-003", "高在庫", "抵抗", 500);

        mockMvc.perform(get("/api/v1/parts")
                        .param("minStock", "10")
                        .param("maxStock", "100")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].stockQuantity").value(50));
    }

    @Test
    void shouldReturnPaginatedResults() throws Exception {
        // 要件 4.4: ページネーション
        for (int i = 1; i <= 15; i++) {
            createAndRegisterPart("PAGE-" + String.format("%03d", i), "ページテスト" + i, "抵抗");
        }

        // 最初のページ
        mockMvc.perform(get("/api/v1/parts")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        // 2番目のページ
        mockMvc.perform(get("/api/v1/parts")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void shouldReturnEmptyListWhenNoPartsMatch() throws Exception {
        // 要件 4.5: 検索条件に一致する部品が存在しない場合
        mockMvc.perform(get("/api/v1/parts")
                        .param("partName", "存在しない部品")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldIncreaseInventory() throws Exception {
        // 要件 5.1, 5.4: 在庫増加
        createAndRegisterPartWithStock("INV-001", "在庫テスト", "抵抗", 100);

        InventoryAdjustmentDto adjustmentDto = new InventoryAdjustmentDto();
        adjustmentDto.setPartCode("INV-001"); // パスパラメータと同じ値を設定
        adjustmentDto.setQuantity(50);
        adjustmentDto.setNotes("入荷による増加");

        mockMvc.perform(put("/api/v1/parts/INV-001/inventory/increase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjustmentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partCode").value("INV-001"))
                .andExpect(jsonPath("$.previousStock").value(100))
                .andExpect(jsonPath("$.newStock").value(150))
                .andExpect(jsonPath("$.adjustmentQuantity").value(50));
    }

    @Test
    void shouldDecreaseInventory() throws Exception {
        // 要件 5.2, 5.4: 在庫減少
        createAndRegisterPartWithStock("INV-002", "在庫減少テスト", "コンデンサ", 100);

        InventoryAdjustmentDto adjustmentDto = new InventoryAdjustmentDto();
        adjustmentDto.setPartCode("INV-002"); // パスパラメータと同じ値を設定
        adjustmentDto.setQuantity(30);
        adjustmentDto.setNotes("出荷による減少");

        mockMvc.perform(put("/api/v1/parts/INV-002/inventory/decrease")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjustmentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partCode").value("INV-002"))
                .andExpect(jsonPath("$.previousStock").value(100))
                .andExpect(jsonPath("$.newStock").value(70))
                .andExpect(jsonPath("$.adjustmentQuantity").value(30));
    }

    @Test
    void shouldReturnBadRequestWhenInventoryBecomesNegative() throws Exception {
        // 要件 5.3: 在庫数量がマイナスになる場合のエラー
        createAndRegisterPartWithStock("INV-003", "在庫不足テスト", "IC", 10);

        InventoryAdjustmentDto adjustmentDto = new InventoryAdjustmentDto();
        adjustmentDto.setPartCode("INV-003"); // パスパラメータと同じ値を設定
        adjustmentDto.setQuantity(20); // 在庫より多い数量
        adjustmentDto.setNotes("在庫不足テスト");

        mockMvc.perform(put("/api/v1/parts/INV-003/inventory/decrease")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjustmentDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("在庫数量が不足")));
    }

    @Test
    void shouldReturnNotFoundWhenInventoryAdjustmentForNonexistentPart() throws Exception {
        // 要件 5.5: 存在しない部品コードでの在庫調整
        InventoryAdjustmentDto adjustmentDto = new InventoryAdjustmentDto();
        adjustmentDto.setPartCode("NONEXISTENT"); // パスパラメータと同じ値を設定
        adjustmentDto.setQuantity(10);

        mockMvc.perform(put("/api/v1/parts/NONEXISTENT/inventory/increase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adjustmentDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("部品が見つかりません")));
    }

    @Test
    void shouldGetLowStockParts() throws Exception {
        // 要件 6.1, 6.2: 低在庫部品一覧（在庫数量昇順）
        createAndRegisterPartWithStockAndThreshold("LOW-001", "低在庫1", "抵抗", 5, 10);
        createAndRegisterPartWithStockAndThreshold("LOW-002", "低在庫2", "コンデンサ", 3, 10);
        createAndRegisterPartWithStockAndThreshold("LOW-003", "十分在庫", "IC", 50, 10);

        mockMvc.perform(get("/api/v1/parts/low-stock")
                        .param("threshold", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].stockQuantity").value(3)) // 昇順確認
                .andExpect(jsonPath("$[1].stockQuantity").value(5))
                .andExpect(jsonPath("$[*].partCode", containsInAnyOrder("LOW-001", "LOW-002")));
    }

    @Test
    void shouldReturnEmptyListWhenNoLowStockParts() throws Exception {
        // 要件 6.3: 低在庫部品が存在しない場合
        createAndRegisterPartWithStockAndThreshold("HIGH-001", "十分在庫", "抵抗", 100, 10);

        mockMvc.perform(get("/api/v1/parts/low-stock")
                        .param("threshold", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldHandleValidationErrors() throws Exception {
        // 要件 7.1: バリデーションエラーの統一エラー形式
        ElectronicPartDto invalidPart = new ElectronicPartDto();
        invalidPart.setPartCode(""); // 空文字
        invalidPart.setPartName(""); // 空文字

        mockMvc.perform(post("/api/v1/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPart)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    void shouldProvideOpenApiDocumentation() throws Exception {
        // 要件 1.4, 2.3, 3.2, 4.4, 5.4, 6.2: OpenAPI/Swagger文書の提供
        
        // OpenAPI JSON仕様書のアクセス確認
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.info.title").value("電子部品在庫管理API"))
                .andExpect(jsonPath("$.info.version").value("v1.0.0"))
                .andExpect(jsonPath("$.paths").exists())
                .andExpect(jsonPath("$.paths['/api/v1/parts']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/parts/{partCode}']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/parts/{partCode}/inventory/increase']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/parts/{partCode}/inventory/decrease']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/parts/low-stock']").exists());
    }

    // ヘルパーメソッド
    private ElectronicPartDto createTestPartDto(String partCode, String partName, String category) {
        ElectronicPartDto dto = new ElectronicPartDto();
        dto.setPartCode(partCode);
        dto.setPartName(partName);
        dto.setCategory(category);
        dto.setSpecifications("テスト仕様");
        dto.setUnitPrice(new BigDecimal("100.00"));
        dto.setStockQuantity(100);
        dto.setLowStockThreshold(10);
        return dto;
    }

    private void createAndRegisterPart(String partCode, String partName, String category) throws Exception {
        ElectronicPartDto partDto = createTestPartDto(partCode, partName, category);
        mockMvc.perform(post("/api/v1/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partDto)))
                .andExpect(status().isCreated());
    }

    private void createAndRegisterPartWithStock(String partCode, String partName, String category, int stockQuantity) throws Exception {
        ElectronicPartDto partDto = createTestPartDto(partCode, partName, category);
        partDto.setStockQuantity(stockQuantity);
        mockMvc.perform(post("/api/v1/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partDto)))
                .andExpect(status().isCreated());
    }

    private void createAndRegisterPartWithStockAndThreshold(String partCode, String partName, String category, 
                                                          int stockQuantity, int threshold) throws Exception {
        ElectronicPartDto partDto = createTestPartDto(partCode, partName, category);
        partDto.setStockQuantity(stockQuantity);
        partDto.setLowStockThreshold(threshold);
        mockMvc.perform(post("/api/v1/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partDto)))
                .andExpect(status().isCreated());
    }
}