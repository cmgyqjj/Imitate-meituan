package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.entity.TradeGoods;
import com.hmdp.entity.TradeUserMoneyLog;
import com.hmdp.mapper.TradeGoodsMapper;
import com.hmdp.mapper.TradeUserMoneyLogMapper;
import com.hmdp.service.IGoodsService;
import com.hmdp.service.ITradeUserMoneyLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @author:{QJJ}
 * @date:{2022}
 * @description:
 **/
@Service
public class GoodsService  extends ServiceImpl<TradeGoodsMapper, TradeGoods> implements IGoodsService {


}
