package com.example.inventory.controller;

import com.example.inventory.dto.ElectronicPartDto;
import com.example.inventory.dto.InventoryAdjustmentDto;
import com.example.inventory.dto.InventoryAdjustmentResponseDto;
import com.example.inventory.exception.ErrorResponse;
import com.example.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在庫管理のRESTコントローラ
 * 在庫調整、低在庫部品検索機能を提供
 */
@RestController
@RequestMapping("/api/v1/parts")
@Tag(name = "在庫管理", description = "電子部品の在庫調整と低在庫部品検索機能を提供するAPI")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * 在庫数量を増加
     * 要件: 5.1, 5.4
     * @param partCode 部品コード
     * @param adjustmentDto 在庫調整情報
     * @return 在庫調整結果（200 OK）
     */
    @Operation(
        summary = "在庫数量増加",
        description = "指定された部品の在庫数量を増加させます。入荷や返品時に使用します。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "在庫増加成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = InventoryAdjustmentResponseDto.class),
                examples = @ExampleObject(
                    name = "在庫増加成功例",
                    value = """
                        {
                          "partCode": "R001",
                          "partName": "抵抗 1kΩ",
                          "previousStock": 85,
                          "newStock": 135,
                          "adjustmentQuantity": 50,
                          "adjustmentType": "INCREASE",
                          "adjustmentDate": "2024-01-01T16:30:00",
                          "notes": "新規入荷"
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
                          "message": "数量は1以上で入力してください",
                          "path": "/api/v1/parts/R001/inventory/increase"
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
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PutMapping("/{partCode}/inventory/increase")
    public ResponseEntity<InventoryAdjustmentResponseDto> increaseStock(
            @Parameter(description = "在庫を増加する部品のコード", required = true, example = "R001")
            @PathVariable String partCode,
            @Parameter(description = "在庫調整情報", required = true)
            @Valid @RequestBody InventoryAdjustmentDto adjustmentDto) {
        
        // パスパラメータの部品コードをDTOに設定
        adjustmentDto.setPartCode(partCode);
        
        InventoryAdjustmentResponseDto response = inventoryService.increaseStock(adjustmentDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 在庫数量を減少
     * 要件: 5.2, 5.3, 5.4, 5.5
     * @param partCode 部品コード
     * @param adjustmentDto 在庫調整情報
     * @return 在庫調整結果（200 OK）
     */
    @Operation(
        summary = "在庫数量減少",
        description = "指定された部品の在庫数量を減少させます。出荷や使用時に使用します。在庫数量がマイナスになる場合はエラーを返します。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "在庫減少成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = InventoryAdjustmentResponseDto.class),
                examples = @ExampleObject(
                    name = "在庫減少成功例",
                    value = """
                        {
                          "partCode": "R001",
                          "partName": "抵抗 1kΩ",
                          "previousStock": 135,
                          "newStock": 110,
                          "adjustmentQuantity": 25,
                          "adjustmentType": "DECREASE",
                          "adjustmentDate": "2024-01-01T17:00:00",
                          "notes": "製品組み立てで使用"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "在庫不足エラー",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "在庫不足エラー例",
                    value = """
                        {
                          "timestamp": "2024-01-01T10:00:00",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "在庫が不足しています。現在の在庫: 10, 要求数量: 50",
                          "path": "/api/v1/parts/R001/inventory/decrease"
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
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PutMapping("/{partCode}/inventory/decrease")
    public ResponseEntity<InventoryAdjustmentResponseDto> decreaseStock(
            @Parameter(description = "在庫を減少する部品のコード", required = true, example = "R001")
            @PathVariable String partCode,
            @Parameter(description = "在庫調整情報", required = true)
            @Valid @RequestBody InventoryAdjustmentDto adjustmentDto) {
        
        // パスパラメータの部品コードをDTOに設定
        adjustmentDto.setPartCode(partCode);
        
        InventoryAdjustmentResponseDto response = inventoryService.decreaseStock(adjustmentDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 低在庫部品一覧を取得
     * 要件: 6.1, 6.2, 6.3
     * @param threshold 在庫閾値（オプション）
     * @return 低在庫部品のリスト（200 OK）
     */
    @Operation(
        summary = "低在庫部品一覧取得",
        description = "在庫数量が閾値以下の部品一覧を取得します。閾値を指定しない場合は、各部品の低在庫閾値を使用します。結果は在庫数量の昇順でソートされます。"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "低在庫部品一覧取得成功",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ElectronicPartDto.class)),
                examples = @ExampleObject(
                    name = "低在庫部品一覧例",
                    value = """
                        [
                          {
                            "partCode": "C001",
                            "partName": "コンデンサ 100μF",
                            "category": "コンデンサ",
                            "specifications": "16V, 電解",
                            "unitPrice": 25.00,
                            "stockQuantity": 5,
                            "lowStockThreshold": 10,
                            "createdAt": "2024-01-01T10:00:00",
                            "updatedAt": "2024-01-01T15:30:00"
                          },
                          {
                            "partCode": "R002",
                            "partName": "抵抗 10kΩ",
                            "category": "抵抗",
                            "specifications": "1/4W, ±5%",
                            "unitPrice": 12.00,
                            "stockQuantity": 15,
                            "lowStockThreshold": 20,
                            "createdAt": "2024-01-01T10:00:00",
                            "updatedAt": "2024-01-01T15:30:00"
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "閾値パラメータエラー",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "閾値エラー例",
                    value = """
                        {
                          "timestamp": "2024-01-01T10:00:00",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "閾値は0以上で指定してください",
                          "path": "/api/v1/parts/low-stock"
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/low-stock")
    public ResponseEntity<List<ElectronicPartDto>> getLowStockParts(
            @Parameter(description = "在庫閾値（指定しない場合は各部品の低在庫閾値を使用）", example = "20")
            @RequestParam(required = false) Integer threshold) {
        
        List<ElectronicPartDto> lowStockParts;
        
        if (threshold != null) {
            // 指定された閾値を使用
            lowStockParts = inventoryService.findLowStockPartsByThreshold(threshold);
        } else {
            // 各部品の低在庫閾値を使用
            lowStockParts = inventoryService.findLowStockParts();
        }
        
        return ResponseEntity.ok(lowStockParts);
    }
}