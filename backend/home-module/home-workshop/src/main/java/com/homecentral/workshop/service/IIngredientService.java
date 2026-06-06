package com.homecentral.workshop.service;

import com.homecentral.workshop.api.vo.IngredientVO;

import java.util.List;

public interface IIngredientService {

    /**
     * 拉所有原料(给搜索框用,~1500 条)
     * 不分页,数据量小,一次性返回。
     */
    List<IngredientVO> listAll();
}
