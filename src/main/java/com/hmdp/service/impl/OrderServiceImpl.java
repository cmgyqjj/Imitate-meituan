package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.Exception.CastException;
import com.hmdp.constant.ShopCode;
import com.hmdp.dto.Result;
import com.hmdp.entity.*;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.mapper.TradeOrderMapper;
import com.hmdp.service.ICouponService;
import com.hmdp.service.IGoodsService;
import com.hmdp.service.IOrderService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.IDWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author:{QJJ}
 * @date:{2022}
 * @description:
 **/
@Slf4j
@Component
@Service
public class OrderServiceImpl extends ServiceImpl<TradeOrderMapper, TradeOrder> implements IOrderService {

    @Resource
    private IUserService userService;

    @Resource
    private IGoodsService goodsService;

    @Resource
    private IDWorker idWorker;
    @Resource
    private ICouponService couponService;

    @Override
    public Result confirmOrder(TradeOrder order) {
        return null;
    }



    /**
     * 扣减库存
     * @param order
     */
    //TODO 这个方法有可能需要加锁，
    // 两个线程同时扣减库存的时候，可能会导致并发问题
    private void reduceGoodsNum(TradeOrder order) {
        TradeGoodsNumberLog goodsNumberLog = new TradeGoodsNumberLog();
        goodsNumberLog.setOrderId(order.getOrderId());
        goodsNumberLog.setGoodsId(order.getGoodsId());
        goodsNumberLog.setGoodsNumber(order.getGoodsNumber());
        Result result = goodsService.reduceGoodsNum(goodsNumberLog);
        if(result.getSuccess().equals(ShopCode.SHOP_FAIL.getSuccess())){
            CastException.cast(ShopCode.SHOP_REDUCE_GOODS_NUM_FAIL);
        }
        log.info("订单:"+order.getOrderId()+"扣减库存成功");
    }

    /**
     * 生成预订单
     * @param order
     * @return
     */
    private Long savePreOrder(TradeOrder order) {
        //1.设置订单状态为不可见
        order.setOrderStatus(ShopCode.SHOP_ORDER_NO_CONFIRM.getCode());
        //2.订单ID
        //利用雪花算法生成的随机id
        order.setOrderId(idWorker.nextId());
        //核算运费是否正确
        //TODO 这里核算运费单纯根据订单的价格,应该改为根据重量路程和订单价格的比
        BigDecimal shippingFee = calculateShippingFee(order.getOrderAmount());
        //这里主要是计算订单运费和设置的订单运费是否一样
        if (order.getShippingFee().compareTo(shippingFee) != 0) {
            CastException.cast(ShopCode.SHOP_ORDER_SHIPPINGFEE_INVALID);
        }
        //3.计算订单总价格是否正确
        //TODO 这里商品数量*商品价格得到的订单价格,需要进行调整,后续可能需要加入购物车的功能
        BigDecimal orderAmount = order.getGoodsPrice().multiply(new BigDecimal(order.getGoodsNumber()));
        orderAmount.add(shippingFee);
        //计算一下算出来的总价和订单价格是否一样
        if (orderAmount.compareTo(order.getOrderAmount()) != 0) {
            CastException.cast(ShopCode.SHOP_ORDERAMOUNT_INVALID);
        }

        //4.判断优惠券信息是否合法
        Long couponId = order.getCouponId();
        if (couponId != null) {
            //通过优惠券id获取到优惠券
            TradeCoupon coupon = couponService.findOne(couponId);
            //优惠券不存在
            if (coupon == null) {
                CastException.cast(ShopCode.SHOP_COUPON_NO_EXIST);
            }
            //优惠券已经使用，优惠券已经使用就是所谓的code值=1
            if ((ShopCode.SHOP_COUPON_ISUSED.getCode().toString())
                    .equals(coupon.getIsUsed().toString())) {
                CastException.cast(ShopCode.SHOP_COUPON_INVALIED);
            }
            //如果优惠券没用,那就把优惠券的单价设置到订单中
            order.setCouponPaid(coupon.getCouponPrice());
        } else {
            //没使用优惠券的话,就设置订单价格为0
            order.setCouponPaid(BigDecimal.ZERO);
        }

        //5.判断余额是否正确
        //这里主要是处理用余额支付的情况,主要支付宝支付接口接入不进来,没办法申请到api
        BigDecimal moneyPaid = order.getMoneyPaid();
        if (moneyPaid != null) {
            //比较余额是否大于0
            int r = order.getMoneyPaid().compareTo(BigDecimal.ZERO);
            //余额小于0
            if (r == -1) {
                CastException.cast(ShopCode.SHOP_MONEY_PAID_LESS_ZERO);
            }
            //余额大于0
            if (r == 1) {
                //查询用户信息
                User user = userService.getById(order.getUserId());
                if (user == null) {
                    CastException.cast(ShopCode.SHOP_USER_NO_EXIST);
                }
                //比较订单价格是否大于用户账户余额
                if (user.getUserMoney().compareTo(order.getMoneyPaid().longValue()) == -1) {
                    CastException.cast(ShopCode.SHOP_MONEY_PAID_INVALID);
                }
                order.setMoneyPaid(order.getMoneyPaid());
            }
        } else {
            order.setMoneyPaid(BigDecimal.ZERO);
        }
        //计算订单支付总价
        order.setPayAmount(orderAmount.subtract(order.getCouponPaid())
                .subtract(order.getMoneyPaid()));
        //设置订单添加时间
        order.setAddTime(new Date());

        //保存预订单
        int r = baseMapper.insert(order);
        if (ShopCode.SHOP_SUCCESS.getCode() != r) {
            CastException.cast(ShopCode.SHOP_ORDER_SAVE_ERROR);
        }
        log.info("订单:["+order.getOrderId()+"]预订单生成成功");
        return order.getOrderId();
    }

    /**
     * 核算运费
     * @param orderAmount
     * @return
     */
    private BigDecimal calculateShippingFee(BigDecimal orderAmount) {
        if(orderAmount.compareTo(new BigDecimal(100))==1){
            return BigDecimal.ZERO;
        }else{
            return new BigDecimal(10);
        }

    }


    /**
     * 效验订单
     * @param order
     */
    private void checkOrder(TradeOrder order) {
        //1.校验订单是否存在
        if (order == null) {
            CastException.cast(ShopCode.SHOP_ORDER_INVALID);
        }
        //2.校验订单中的商品是否存在
        // TODO 这几个位置可以稍微缓存一下，不用去查数据库
        TradeGoods goods = goodsService.findOne(order.getGoodsId());
        if (goods == null) {
            CastException.cast(ShopCode.SHOP_GOODS_NO_EXIST);
        }
        //3.校验下单用户是否存在
        User user = userService.getById(order.getUserId());
        if (user == null) {
            CastException.cast(ShopCode.SHOP_USER_NO_EXIST);
        }
        //4.校验商品单价是否合法
        if (order.getGoodsPrice().compareTo(goods.getGoodsPrice()) != 0) {
            CastException.cast(ShopCode.SHOP_GOODS_PRICE_INVALID);
        }
        //5.校验订单商品数量是否合法
        if (order.getGoodsNumber() >= goods.getGoodsNumber()) {
            CastException.cast(ShopCode.SHOP_GOODS_NUM_NOT_ENOUGH);
        }

        log.info("校验订单通过");

    }
}

