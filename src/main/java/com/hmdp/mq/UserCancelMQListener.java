package com.hmdp.mq;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hmdp.constant.ShopCode;
import com.hmdp.entity.MQEntity;
import com.hmdp.entity.TradeGoods;
import com.hmdp.entity.TradeGoodsNumberLog;
import com.hmdp.entity.TradeMqConsumerLog;
import com.hmdp.mapper.TradeGoodsMapper;
import com.hmdp.mapper.TradeGoodsNumberLogMapper;
import com.hmdp.mapper.TradeMqConsumerLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Component
@RocketMQMessageListener(topic = "${mq.order.topic}",consumerGroup = "${mq.order.consumer.group}",messageModel = MessageModel.BROADCASTING)
public class UserCancelMQListener implements RocketMQListener<MessageExt> {

    @Override
    public void onMessage(MessageExt messageExt) {

    }
}
