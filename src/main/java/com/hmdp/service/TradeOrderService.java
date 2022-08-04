package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.TradeOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
public interface TradeOrderService extends IService<TradeOrder> {
    /**
     * 下单接口
     * @param order
     * @return
     */
    public Result confirmOrder(TradeOrder order);


}
