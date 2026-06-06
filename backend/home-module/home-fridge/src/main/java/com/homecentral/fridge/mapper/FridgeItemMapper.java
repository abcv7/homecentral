package com.homecentral.fridge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homecentral.fridge.entity.FridgeItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FridgeItemMapper extends BaseMapper<FridgeItem> {
}
