package com.hmdp.mq;

import com.alibaba.fastjson.JSON;
import com.hmdp.entity.MQEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(topic = "${mq.order.topic}",consumerGroup = "${mq.order.consumer.group}",messageModel = MessageModel.BROADCASTING)
public class CouponCancelMQListener implements RocketMQListener<MessageExt> {


    @Override
    public void onMessage(MessageExt messageExt) {
        try{
//        解析消息
            String body=new String(messageExt.getBody(),"UTF-8");
            MQEntity mqEntity = JSON.parseObject(body, MQEntity.class);
            log.info("接受到消息");
//            查询优惠卷信息


        }catch (Exception e){

        }
    }
}
