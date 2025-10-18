package com.example.inventory.service;

import com.example.inventory.dto.ElectronicPartDto;
import com.example.inventory.dto.ElectronicPartMapper;
import com.example.inventory.dto.SearchCriteriaDto;
import com.example.inventory.entity.ElectronicPart;
import com.example.inventory.exception.DuplicatePartCodeException;
import com.example.inventory.exception.PartNotFoundException;
import com.example.inventory.repository.ElectronicPartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 電子部品サービスクラス
 * 電子部品のCRUD操作、検索、ビジネスロジックを提供
 */
@Service
@Transactional
public class ElectronicPartService {

    private final ElectronicPartRepository electronicPartRepository;
    private final ElectronicPartMapper electronicPartMapper;

    @Autowired
    public ElectronicPartService(ElectronicPartRepository electronicPartRepository,
                               ElectronicPartMapper electronicPartMapper) {
        this.electronicPartRepository = electronicPartRepository;
        this.electronicPartMapper = electronicPartMapper;
    }

    /**
     * 新規電子部品を登録
     * 要件: 1.1, 1.2
     * @param electronicPartDto 登録する部品情報
     * @return 登録された部品情報
     * @throws DuplicatePartCodeException 部品コードが重複している場合
     */
    public ElectronicPartDto createElectronicPart(ElectronicPartDto electronicPartDto) {
        // 部品コード重複チェック（要件1.2）
        if (electronicPartRepository.existsByPartCode(electronicPartDto.getPartCode())) {
            throw new DuplicatePartCodeException("部品コード '" + electronicPartDto.getPartCode() + "' は既に存在します");
        }

        ElectronicPart entity = electronicPartMapper.toEntity(electronicPartDto);
        ElectronicPart savedEntity = electronicPartRepository.save(entity);
        return electronicPartMapper.toDto(savedEntity);
    }

    /**
     * 部品コードで電子部品を取得
     * 要件: 2.1
     * @param partCode 部品コード
     * @return 部品情報
     * @throws PartNotFoundException 部品が存在しない場合
     */
    @Transactional(readOnly = true)
    public ElectronicPartDto getElectronicPartByCode(String partCode) {
        ElectronicPart entity = electronicPartRepository.findByPartCode(partCode)
                .orElseThrow(() -> new PartNotFoundException("部品コード '" + partCode + "' の部品が見つかりません"));
        return electronicPartMapper.toDto(entity);
    }

    /**
     * 電子部品情報を更新
     * 要件: 2.1, 2.2, 2.3
     * @param partCode 更新対象の部品コード
     * @param electronicPartDto 更新内容
     * @return 更新された部品情報
     * @throws PartNotFoundException 部品が存在しない場合
     */
    public ElectronicPartDto updateElectronicPart(String partCode, ElectronicPartDto electronicPartDto) {
        ElectronicPart existingEntity = electronicPartRepository.findByPartCode(partCode)
                .orElseThrow(() -> new PartNotFoundException("部品コード '" + partCode + "' の部品が見つかりません"));

        // 部品コードの変更は許可しない（要件2.4）
        electronicPartDto.setPartCode(partCode);
        
        electronicPartMapper.updateEntityFromDto(existingEntity, electronicPartDto);
        ElectronicPart updatedEntity = electronicPartRepository.save(existingEntity);
        return electronicPartMapper.toDto(updatedEntity);
    }

    /**
     * 電子部品を削除
     * 要件: 3.1, 3.2
     * @param partCode 削除対象の部品コード
     * @throws PartNotFoundException 部品が存在しない場合
     */
    public void deleteElectronicPart(String partCode) {
        if (!electronicPartRepository.existsByPartCode(partCode)) {
            throw new PartNotFoundException("部品コード '" + partCode + "' の部品が見つかりません");
        }
        electronicPartRepository.deleteByPartCode(partCode);
    }

    /**
     * 電子部品を検索（ページネーション対応）
     * 要件: 4.1, 4.2, 4.3, 4.4, 4.5
     * @param searchCriteria 検索条件
     * @return 検索結果のPage
     */
    @Transactional(readOnly = true)
    public Page<ElectronicPartDto> searchElectronicParts(SearchCriteriaDto searchCriteria) {
        // 在庫数量範囲の検証
        if (!searchCriteria.isValidStockRange()) {
            throw new IllegalArgumentException("最小在庫数は最大在庫数以下である必要があります");
        }

        Pageable pageable = PageRequest.of(
                searchCriteria.getPage(),
                searchCriteria.getSize(),
                Sort.by("partCode").ascending()
        );

        Page<ElectronicPart> entityPage = electronicPartRepository.findBySearchCriteria(
                searchCriteria.getPartName(),
                searchCriteria.getCategory(),
                searchCriteria.getMinStock(),
                searchCriteria.getMaxStock(),
                pageable
        );

        return entityPage.map(electronicPartMapper::toDto);
    }

    /**
     * 全ての電子部品を取得（ページネーション対応）
     * @param page ページ番号
     * @param size ページサイズ
     * @return 部品リストのPage
     */
    @Transactional(readOnly = true)
    public Page<ElectronicPartDto> getAllElectronicParts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("partCode").ascending());
        Page<ElectronicPart> entityPage = electronicPartRepository.findAll(pageable);
        return entityPage.map(electronicPartMapper::toDto);
    }

    /**
     * 部品コードの存在確認
     * @param partCode 部品コード
     * @return 存在する場合true
     */
    @Transactional(readOnly = true)
    public boolean existsByPartCode(String partCode) {
        return electronicPartRepository.existsByPartCode(partCode);
    }

    /**
     * カテゴリ別の部品数を取得
     * @return カテゴリ別部品数のリスト
     */
    @Transactional(readOnly = true)
    public List<Object[]> getPartCountByCategory() {
        return electronicPartRepository.countPartsByCategory();
    }

    /**
     * 在庫総数を取得
     * @return 全部品の在庫総数
     */
    @Transactional(readOnly = true)
    public Long getTotalStockQuantity() {
        Long total = electronicPartRepository.getTotalStockQuantity();
        return total != null ? total : 0L;
    }


}