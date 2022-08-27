package com.hmdp.mq;

import com.alibaba.fastjson.JSON;
import com.hmdp.constant.ShopCode;
import com.hmdp.entity.MQEntity;
import com.hmdp.entity.TradeOrder;
import com.hmdp.mapper.TradeOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;

@Component
@Slf4j
@RocketMQMessageListener(topic = "${mq.order.topic}",consumerGroup = "${mq.order.consumer.group}",messageModel = MessageModel.BROADCASTING)
public class OrderCancelMQListener implements RocketMQListener<MessageExt> {

    @Resource
    private TradeOrderMapper orderMapper;


    @Override
    public void onMessage(MessageExt messageExt) {
        try{
//            解析消息内容
            String body = new String(messageExt.getBody(), "UTF-8");
            MQEntity mqEntity= JSON.parseObject(body,MQEntity.class);
            log.info("接受消息成功");
//            查询订单
            TradeOrder tradeOrder = orderMapper.selectById(mqEntity.getOrderId());
//            更新订单状态
            tradeOrder.setOrderStatus(ShopCode.SHOP_ORDER_CANCEL.getCode());
            orderMapper.updateById(tradeOrder);
            log.info("订单"+tradeOrder.getOrderId()+"状态设置为取消");
        }catch (UnsupportedEncodingException e){
            log.info("订单取消异常");
            e.printStackTrace();
        }
    }
}
