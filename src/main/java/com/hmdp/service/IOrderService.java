package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.TradeOrder;

/**
 * @author:{QJJ}
 * @date:{2022}
 * @description:
 **/
public interface IOrderService {

    /**
     * 下单接口
     * @param order
     * @return
     */
    public Result confirmOrder(TradeOrder order);

}