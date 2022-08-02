package com.hmdp.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.Exception.CastException;
import com.hmdp.constant.ShopCode;
import com.hmdp.dto.Result;
import com.hmdp.entity.Blog;
import com.hmdp.entity.TradeCoupon;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.mapper.TradeCouponMapper;
import com.hmdp.service.ICouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Service
public class CouponServiceImpl extends ServiceImpl<TradeCouponMapper, TradeCoupon> implements ICouponService {

    @Autowired
    private TradeCouponMapper couponMapper;

    /**
     * 通过优惠券id查找优惠券
     * @param coupouId
     * @return
     */
    @Override
    public TradeCoupon findOne(Long coupouId) {
        if(coupouId==null){
            CastException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        return couponMapper.selectByPrimaryKey(coupouId);
    }

    /**
     * 更新优惠券状态
     * @param coupon
     * @return
     */
    @Override
    public Result updateCouponStatus(TradeCoupon coupon) {
        if(coupon==null||coupon.getCouponId()==null){
            CastException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }
        //更新优惠券状态
        couponMapper.updateByPrimaryKey(coupon);
        return Result.ok(ShopCode.SHOP_SUCCESS.getMessage());
    }
}
