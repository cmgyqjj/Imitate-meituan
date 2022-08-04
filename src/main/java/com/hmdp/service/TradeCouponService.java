package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.TradeCoupon;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
public interface TradeCouponService extends IService<TradeCoupon> {
    /**
     * 根据ID查询优惠券对象
     * @param coupouId
     * @return
     */
    public TradeCoupon findOne(Long coupouId);

    /**
     * 更细优惠券状态
     * @param coupon
     * @return
     */
    public Result updateCouponStatus(TradeCoupon coupon);

}
