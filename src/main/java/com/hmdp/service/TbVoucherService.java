package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.TbVoucher;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
public interface TbVoucherService extends IService<TbVoucher> {
    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(TbVoucher voucher);

}
