package com.hmdp.mq;

import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(topic = "${mq.order.topic}",consumerGroup = "${mq.order.consumer.group}",messageModel = MessageModel.BROADCASTING)
public class CouponCancelMQListener implements RocketMQListener<MessageExt> {


    @Override
    public void onMessage(MessageExt messageExt) {

    }
}
