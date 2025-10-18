package com.example.inventory.controller;

import com.example.inventory.dto.ElectronicPartDto;
import com.example.inventory.dto.SearchCriteriaDto;
import com.example.inventory.exception.ErrorResponse;
import com.example.inventory.service.ElectronicPartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 電子部品管理のRESTコントローラ
 * 電子部品のCRUD操作、検索機能を提供
 */
@RestController
@RequestMapping("/api/v1/parts")
@Tag(name = "電子部品管理", description = "電子部品のCRUD操作と検索機能を提供するAPI")
public class ElectronicPartController {

    private final ElectronicPartService electronicPartService;

    @Autowired
    public ElectronicPartController(ElectronicPartService electronicPartService) {
        this.electronicPartService = electronicPartService;
    }

    /**
     * 新規電子部品を登録
     * 要件: 1.1, 1.2, 1.3, 1.4
     * @param electronicPartDto 登録する部品情報
     * @return 登録された部品情報（201 Created）
     */
    @Operation(
        summary = "新規電子部品登録",
        description = "新しい電子部品をシステムに登録します。部品コードは一意である必要があります。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "部品登録成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ElectronicPartDto.class),
                examples = @ExampleObject(
                    name = "登録成功例",
                    value = """
                        {
                          "partCode": "R001",
                          "partName": "抵抗 1kΩ",
                          "category": "抵抗",
                          "specifications": "1/4W, ±5%",
                          "unitPrice": 10.50,
                          "stockQuantity": 100,
                          "lowStockThreshold": 20,
                          "createdAt": "2024-01-01T10:00:00",
                          "updatedAt": "2024-01-01T10:00:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "バリデーションエラー",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "バリデーションエラー例",
                    value = """
                        {
                          "timestamp": "2024-01-01T10:00:00",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "部品コードは必須です",
                          "path": "/api/v1/parts"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "部品コード重複エラー",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "重複エラー例",
                    value = """
                        {
                          "timestamp": "2024-01-01T10:00:00",
                          "status": 409,
                          "error": "Conflict",
                          "message": "部品コード 'R001' は既に存在します",
                          "path": "/api/v1/parts"
                        }
                        """
                )
            )
        )
    })
    @PostMapping
    public ResponseEntity<ElectronicPartDto> createElectronicPart(
            @Parameter(description = "登録する部品情報", required = true)
            @Valid @RequestBody ElectronicPartDto electronicPartDto) {
        ElectronicPartDto createdPart = electronicPartService.createElectronicPart(electronicPartDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPart);
    }

    /**
     * 部品コードで電子部品を取得
     * 要件: 2.1
     * @param partCode 部品コード
     * @return 部品情報（200 OK）
     */
    @Operation(
        summary = "電子部品詳細取得",
        description = "指定された部品コードの電子部品詳細情報を取得します。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "部品情報取得成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ElectronicPartDto.class),
                examples = @ExampleObject(
                    name = "取得成功例",
                    value = """
                        {
                          "partCode": "R001",
                          "partName": "抵抗 1kΩ",
                          "category": "抵抗",
                          "specifications": "1/4W, ±5%",
                          "unitPrice": 10.50,
                          "stockQuantity": 85,
                          "lowStockThreshold": 20,
                          "createdAt": "2024-01-01T10:00:00",
                          "updatedAt": "2024-01-01T15:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "部品が見つからない",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "部品未発見例",
                    value = """
                        {
                          "timestamp": "2024-01-01T10:00:00",
                          "status": 404,
                          "error": "Not Found",
                          "message": "部品コード 'R999' が見つかりません",
                          "path": "/api/v1/parts/R999"
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/{partCode}")
    public ResponseEntity<ElectronicPartDto> getElectronicPart(
            @Parameter(description = "取得する部品のコード", required = true, example = "R001")
            @PathVariable String partCode) {
        ElectronicPartDto part = electronicPartService.getElectronicPartByCode(partCode);
        return ResponseEntity.ok(part);
    }

    /**
     * 電子部品情報を更新
     * 要件: 2.1, 2.2, 2.3
     * @param partCode 更新対象の部品コード
     * @param electronicPartDto 更新内容
     * @return 更新された部品情報（200 OK）
     */
    @Operation(
        summary = "電子部品情報更新",
        description = "指定された部品コードの電子部品情報を更新します。部品コード自体は変更できません。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "部品情報更新成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ElectronicPartDto.class),
                examples = @ExampleObject(
                    name = "更新成功例",
                    value = """
                        {
                          "partCode": "R001",
                          "partName": "抵抗 1kΩ (高精度)",
                          "category": "抵抗",
                          "specifications": "1/4W, ±1%",
                          "unitPrice": 15.00,
                          "stockQuantity": 85,
                          "lowStockThreshold": 25,
                          "createdAt": "2024-01-01T10:00:00",
                          "updatedAt": "2024-01-01T16:00:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "バリデーションエラー",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "部品が見つからない",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PutMapping("/{partCode}")
    public ResponseEntity<ElectronicPartDto> updateElectronicPart(
            @Parameter(description = "更新する部品のコード", required = true, example = "R001")
            @PathVariable String partCode,
            @Parameter(description = "更新する部品情報", required = true)
            @Valid @RequestBody ElectronicPartDto electronicPartDto) {
        ElectronicPartDto updatedPart = electronicPartService.updateElectronicPart(partCode, electronicPartDto);
        return ResponseEntity.ok(updatedPart);
    }

    /**
     * 電子部品を削除
     * 要件: 3.1, 3.2
     * @param partCode 削除対象の部品コード
     * @return 削除成功（204 No Content）
     */
    @Operation(
        summary = "電子部品削除",
        description = "指定された部品コードの電子部品をシステムから削除します。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "部品削除成功"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "部品が見つからない",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "部品未発見例",
                    value = """
                        {
                          "timestamp": "2024-01-01T10:00:00",
                          "status": 404,
                          "error": "Not Found",
                          "message": "部品コード 'R999' が見つかりません",
                          "path": "/api/v1/parts/R999"
                        }
                        """
                )
            )
        )
    })
    @DeleteMapping("/{partCode}")
    public ResponseEntity<Void> deleteElectronicPart(
            @Parameter(description = "削除する部品のコード", required = true, example = "R001")
            @PathVariable String partCode) {
        electronicPartService.deleteElectronicPart(partCode);
        return ResponseEntity.noContent().build();
    }

    /**
     * 電子部品を検索（ページネーション対応）
     * 要件: 4.1, 4.2, 4.3, 4.4, 4.5
     * @param partName 部品名（部分一致検索）
     * @param category カテゴリ（完全一致検索）
     * @param minStock 最小在庫数
     * @param maxStock 最大在庫数
     * @param page ページ番号（デフォルト: 0）
     * @param size ページサイズ（デフォルト: 20）
     * @return 検索結果のPage（200 OK）
     */
    @Operation(
        summary = "電子部品検索",
        description = "指定された条件で電子部品を検索します。すべての検索条件はオプションで、ページネーション機能を提供します。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "検索成功",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "検索結果例",
                    value = """
                        {
                          "content": [
                            {
                              "partCode": "R001",
                              "partName": "抵抗 1kΩ",
                              "category": "抵抗",
                              "specifications": "1/4W, ±5%",
                              "unitPrice": 10.50,
                              "stockQuantity": 85,
                              "lowStockThreshold": 20,
                              "createdAt": "2024-01-01T10:00:00",
                              "updatedAt": "2024-01-01T15:30:00"
                            }
                          ],
                          "pageable": {
                            "pageNumber": 0,
                            "pageSize": 20,
                            "sort": {
                              "empty": true,
                              "sorted": false,
                              "unsorted": true
                            }
                          },
                          "totalElements": 1,
                          "totalPages": 1,
                          "last": true,
                          "first": true,
                          "numberOfElements": 1,
                          "size": 20,
                          "number": 0,
                          "sort": {
                            "empty": true,
                            "sorted": false,
                            "unsorted": true
                          },
                          "empty": false
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "検索条件エラー",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping
    public ResponseEntity<Page<ElectronicPartDto>> searchElectronicParts(
            @Parameter(description = "部品名（部分一致検索）", example = "抵抗")
            @RequestParam(required = false) String partName,
            @Parameter(description = "カテゴリ（完全一致検索）", example = "抵抗")
            @RequestParam(required = false) String category,
            @Parameter(description = "最小在庫数", example = "10")
            @RequestParam(required = false) Integer minStock,
            @Parameter(description = "最大在庫数", example = "100")
            @RequestParam(required = false) Integer maxStock,
            @Parameter(description = "ページ番号（0から開始）", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "ページサイズ（1-100）", example = "20")
            @RequestParam(defaultValue = "20") Integer size) {
        
        SearchCriteriaDto searchCriteria = new SearchCriteriaDto(
                partName, category, minStock, maxStock, page, size);
        
        Page<ElectronicPartDto> result = electronicPartService.searchElectronicParts(searchCriteria);
        return ResponseEntity.ok(result);
    }
}