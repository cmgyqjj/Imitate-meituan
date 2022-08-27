package com.hmdp.mq;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hmdp.constant.ShopCode;
import com.hmdp.entity.*;
import com.hmdp.mapper.TradeGoodsMapper;
import com.hmdp.mapper.TradeGoodsNumberLogMapper;
import com.hmdp.mapper.TradeMqConsumerLogMapper;
import com.hmdp.service.TbUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;

@Slf4j
@Component
@RocketMQMessageListener(topic = "${mq.order.topic}",consumerGroup = "${mq.order.consumer.group}",messageModel = MessageModel.BROADCASTING)
public class UserCancelMQListener implements RocketMQListener<MessageExt> {

    @Resource
    private TbUserService userService;

    @Override
    public void onMessage(MessageExt messageExt) {
//        解析消息
        try {
            String body = new String(messageExt.getBody(), "UTF-8");
            MQEntity mqEntity = JSON.parseObject(body, MQEntity.class);
            log.info("接受到消息");
            if(mqEntity.getUserMoney()!=null&&mqEntity.getUserMoney().compareTo(BigDecimal.ZERO)>0){
//                调用业务层，进行余额修改
                TradeUserMoneyLog tradeUserMoneyLog = new TradeUserMoneyLog();
                tradeUserMoneyLog.setUseMoney(mqEntity.getUserMoney());
                tradeUserMoneyLog.setMoneyLogType(ShopCode.SHOP_USER_MONEY_REFUND.getCode());
                tradeUserMoneyLog.setUserId(mqEntity.getUserId());
                tradeUserMoneyLog.setOrderId(mqEntity.getOrderId());
                userService.updateMoneyPaid(tradeUserMoneyLog);
                log.info("余额回退成功");
            }
        } catch (UnsupportedEncodingException e) {
            log.info("余额回退失败");
            throw new RuntimeException(e);
        }
    }
}
