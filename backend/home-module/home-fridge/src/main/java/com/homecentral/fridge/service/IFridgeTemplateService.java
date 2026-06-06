package com.homecentral.fridge.service;

import com.homecentral.fridge.api.dto.FridgeTemplateRequest;
import com.homecentral.fridge.api.vo.FridgeTemplateVO;

import java.util.List;

public interface IFridgeTemplateService {

    /**
     * 列出当前用户可见的模板：
     * <ul>
     *     <li>4 套系统预置模板（is_system = TRUE，owner_id IS NULL）</li>
     *     <li>当前用户自己创建的模板</li>
     * </ul>
     * 系统预置在前，按 id ASC；用户模板按 is_default DESC, name ASC。
     */
    List<FridgeTemplateVO> listVisible(Long userId);

    /**
     * 获取当前用户激活的默认模板。
     * 若用户尚未激活，则返回第一个系统预置模板（按 id ASC）。
     */
    FridgeTemplateVO getDefault(Long userId);

    /**
     * 创建用户自定义模板。
     * 名称在当前用户下不可重；创建后 is_default = false（用户需主动 activate 切换）。
     */
    FridgeTemplateVO create(FridgeTemplateRequest request, Long userId);

    /**
     * 更新用户模板（系统预置不可改）。
     */
    FridgeTemplateVO update(Long id, FridgeTemplateRequest request, Long userId);

    /**
     * 删除用户模板（系统预置不可删；当前激活默认模板不可删）。
     */
    void delete(Long id, Long userId);

    /**
     * 激活指定模板为当前用户的默认模板。
     * 事务内：先把当前用户所有模板的 is_default 清零，再把目标模板置为 is_default = true。
     * 若目标模板是系统预置（owner_id = NULL），会自动克隆一份用户专属模板并激活，
     * 避免后续调整影响系统预置的全局数据。
     */
    FridgeTemplateVO activate(Long id, Long userId);
}
