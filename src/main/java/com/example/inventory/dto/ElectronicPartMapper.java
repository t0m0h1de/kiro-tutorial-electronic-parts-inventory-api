package com.example.inventory.dto;

import com.example.inventory.entity.ElectronicPart;
import org.springframework.stereotype.Component;

/**
 * ElectronicPartエンティティとElectronicPartDto間のマッピングクラス
 * エンティティとDTOの相互変換を行う
 */
@Component
public class ElectronicPartMapper {

    /**
     * ElectronicPartエンティティをElectronicPartDtoに変換
     * @param entity 変換元のエンティティ
     * @return 変換されたDTO
     */
    public ElectronicPartDto toDto(ElectronicPart entity) {
        if (entity == null) {
            return null;
        }

        ElectronicPartDto dto = new ElectronicPartDto();
        dto.setPartCode(entity.getPartCode());
        dto.setPartName(entity.getPartName());
        dto.setCategory(entity.getCategory());
        dto.setSpecifications(entity.getSpecifications());
        dto.setUnitPrice(entity.getUnitPrice());
        dto.setStockQuantity(entity.getStockQuantity());
        dto.setLowStockThreshold(entity.getLowStockThreshold());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    /**
     * ElectronicPartDtoをElectronicPartエンティティに変換
     * @param dto 変換元のDTO
     * @return 変換されたエンティティ
     */
    public ElectronicPart toEntity(ElectronicPartDto dto) {
        if (dto == null) {
            return null;
        }

        ElectronicPart entity = new ElectronicPart();
        entity.setPartCode(dto.getPartCode());
        entity.setPartName(dto.getPartName());
        entity.setCategory(dto.getCategory());
        entity.setSpecifications(dto.getSpecifications());
        entity.setUnitPrice(dto.getUnitPrice());
        entity.setStockQuantity(dto.getStockQuantity());
        entity.setLowStockThreshold(dto.getLowStockThreshold());
        // createdAt, updatedAtはJPAライフサイクルメソッドで自動設定

        return entity;
    }

    /**
     * 既存のElectronicPartエンティティをElectronicPartDtoの内容で更新
     * @param entity 更新対象のエンティティ
     * @param dto 更新内容のDTO
     */
    public void updateEntityFromDto(ElectronicPart entity, ElectronicPartDto dto) {
        if (entity == null || dto == null) {
            return;
        }

        // 部品コードは更新不可（要件2.4）
        entity.setPartName(dto.getPartName());
        entity.setCategory(dto.getCategory());
        entity.setSpecifications(dto.getSpecifications());
        entity.setUnitPrice(dto.getUnitPrice());
        entity.setStockQuantity(dto.getStockQuantity());
        entity.setLowStockThreshold(dto.getLowStockThreshold());
        // updatedAtはJPAライフサイクルメソッドで自動更新
    }
}