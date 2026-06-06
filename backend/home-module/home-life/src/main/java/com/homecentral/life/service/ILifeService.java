package com.homecentral.life.service;

import com.homecentral.life.api.dto.AnniversaryCreateRequest;
import com.homecentral.life.api.dto.ReminderRuleCreateRequest;
import com.homecentral.life.api.dto.ShoppingMemoCreateRequest;
import com.homecentral.life.api.vo.AnniversaryVO;
import com.homecentral.life.api.vo.ReminderRuleVO;
import com.homecentral.life.api.vo.ShoppingMemoVO;

import java.util.List;

public interface ILifeService {

    ShoppingMemoVO createShoppingMemo(ShoppingMemoCreateRequest request, Long userId);

    List<ShoppingMemoVO> listShoppingMemos(Long userId);

    ShoppingMemoVO togglePurchased(Long id, Long userId);

    AnniversaryVO createAnniversary(AnniversaryCreateRequest request, Long userId);

    List<AnniversaryVO> listAnniversaries(Long userId);

    ReminderRuleVO createReminderRule(ReminderRuleCreateRequest request, Long userId);

    List<ReminderRuleVO> listReminderRules(Long userId);

    ReminderRuleVO toggleEnabled(Long id, Long userId);
}
