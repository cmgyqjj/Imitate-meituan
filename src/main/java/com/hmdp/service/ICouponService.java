package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.Result;
import com.hmdp.entity.Blog;
import com.hmdp.entity.TradeCoupon;


/**
 * 优惠券接口
 */
public interface ICouponService extends IService<TradeCoupon> {


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
