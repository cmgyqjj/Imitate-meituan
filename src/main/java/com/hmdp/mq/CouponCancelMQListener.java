package com.hmdp.mq;

import com.alibaba.fastjson.JSON;
import com.hmdp.constant.ShopCode;
import com.hmdp.entity.MQEntity;
import com.hmdp.entity.TradeCoupon;
import com.hmdp.mapper.TradeCouponMapper;
import lombok.extern.slf4j.Slf4j;

import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
@RocketMQMessageListener(topic = "${mq.order.topic}",consumerGroup = "${mq.order.consumer.group}",messageModel = MessageModel.BROADCASTING)
public class CouponCancelMQListener implements RocketMQListener<MessageExt> {

    @Resource
    private TradeCouponMapper tradeCouponMapper;

    @Override
    public void onMessage(MessageExt messageExt) {
        try{
//        解析消息
            String body=new String(messageExt.getBody(),"UTF-8");
            MQEntity mqEntity = JSON.parseObject(body, MQEntity.class);
            log.info("接受到消息");
            if(mqEntity.getCouponId()!=null){
//            查询优惠卷信息
                TradeCoupon coupon = tradeCouponMapper.selectById(mqEntity.getCouponId());
                coupon.setUsedTime(null);
                coupon.setIsUsed(ShopCode.SHOP_COUPON_UNUSED.getCode());
                coupon.setOrderId(null);
                tradeCouponMapper.updateById(coupon);
                log.info("回退优惠卷成功");
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("回退优惠卷失败");
        }
    }
}
