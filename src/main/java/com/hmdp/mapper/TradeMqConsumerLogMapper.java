package com.hmdp.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmdp.entity.TradeGoodsNumberLog;
import com.hmdp.entity.TradeMqConsumerLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TradeMqConsumerLogMapper extends BaseMapper<TradeMqConsumerLog> {

}