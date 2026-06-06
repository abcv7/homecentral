package com.homecentral.fridge.service;

import com.homecentral.fridge.api.dto.FridgeCategoryRequest;
import com.homecentral.fridge.api.vo.FridgeCategoryVO;

import java.util.List;

public interface IFridgeCategoryService {

    /**
     * 列出当前用户可见的分类：
     * <ul>
     *     <li>系统预置分类（is_system = TRUE，created_by IS NULL）</li>
     *     <li>当前用户自己创建的分类</li>
     * </ul>
     * 按 sort_order ASC, id ASC 排序。
     */
    List<FridgeCategoryVO> listVisible(Long userId);

    FridgeCategoryVO create(FridgeCategoryRequest request, Long userId);

    FridgeCategoryVO update(Long id, FridgeCategoryRequest request, Long userId);

    void delete(Long id, Long userId);

    /**
     * 按 ID 列表加载分类，用于在识别结果中按 suggestedCategoryName 匹配。
     */
    List<FridgeCategoryVO> findAll();
}
