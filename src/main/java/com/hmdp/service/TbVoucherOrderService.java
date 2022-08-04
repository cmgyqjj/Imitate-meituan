package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.TbVoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
public interface TbVoucherOrderService extends IService<TbVoucherOrder> {
    Result seckillVoucher(Long voucherId);

    void createVoucheOrder(TbVoucherOrder voucherId);

}
