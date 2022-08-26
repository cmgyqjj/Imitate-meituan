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


@Component
@Slf4j
@RocketMQMessageListener(topic = "${mq.order.topic}",consumerGroup = "${mq.order.consumer.group}",messageModel = MessageModel.BROADCASTING)
public class GoodCancelMQListener implements RocketMQListener<MessageExt> {


    @Value("${mq.order.consumer.group}")
    private String groupName;

    @Resource
    private TradeMqConsumerLogMapper mqConsumerLogMapper;
    @Resource
    private TradeGoodsMapper goodsMapper;
    @Resource
    private TradeGoodsNumberLogMapper goodsNumberLogMapper;

    @Override
    public void onMessage(MessageExt messageExt) {
        String msgId=null;
        String tags=null;
        String keys=null;
        String body = null;
//        解析消息内容
        try{
            msgId=messageExt.getMsgId();
            tags=messageExt.getTags();
            keys=messageExt.getKeys();
            body = new String(messageExt.getBody(),"UTF-8");
//        查询消息消费记录
            QueryWrapper wrapper=new QueryWrapper();
            wrapper.eq("msg_tag",tags);
            wrapper.eq("msg_keys",keys);
            wrapper.eq("group_name",groupName);
            TradeMqConsumerLog tradeMqConsumerLog = mqConsumerLogMapper.selectOne(wrapper);
//        判断是否消费过
            if(tradeMqConsumerLog!=null){
//                获得消息处理状态
                Integer consumerStatus = tradeMqConsumerLog.getConsumerStatus();
//                如果处理过 返回
                if(ShopCode.SHOP_MQ_MESSAGE_STATUS_SUCCESS.getCode().intValue()==consumerStatus.intValue()){
                    log.info("消息"+msgId+"已经处理成功");
                    return;
                }
//                正在处理 返回
                if(ShopCode.SHOP_MQ_MESSAGE_STATUS_PROCESSING.getCode().intValue()==consumerStatus.intValue()){
                    log.info("消息"+msgId+"正在处理");
                    return;
                }
//                处理失败
                if(ShopCode.SHOP_MQ_MESSAGE_STATUS_PROCESSING.getCode().intValue()==consumerStatus.intValue()){
//                    获得消息处理的次数
                    Integer consumerTimes = tradeMqConsumerLog.getConsumerTimes();
                    if(consumerTimes>3){
                        log.info("消息"+msgId+"处理超过三次不能继续处理");
                        return;
                    }
//                    TODO 加一个乐观锁修改mqConsumerLog
//                    mqConsumerLogMapper
                }
            }else{
//        判断没有消费过
                TradeMqConsumerLog insertConsumerLog = new TradeMqConsumerLog();
                insertConsumerLog.setMsgId(msgId);
                insertConsumerLog.setMsgKey(keys);
                insertConsumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_PROCESSING.getCode());
                insertConsumerLog.setMsgBody(body);
                insertConsumerLog.setMsgTag(tags);
                insertConsumerLog.setConsumerTimes(0);
                mqConsumerLogMapper.insert(insertConsumerLog);
            }
//        回退库存
            MQEntity mqEntity = JSON.parseObject(body,MQEntity.class);
            Long goodsId = mqEntity.getGoodsId();
            TradeGoods goods = goodsMapper.selectById(goodsId);
            goods.setGoodsNumber(goods.getGoodsNumber()+mqEntity.getGoodsNum());
            goodsMapper.updateById(goods);
//        记录库存操作日志
            TradeGoodsNumberLog goodsNumberLog = new TradeGoodsNumberLog();
            goodsNumberLog.setOrderId(mqEntity.getOrderId());
            goodsNumberLog.setGoodsId(goodsId);
            goodsNumberLog.setGoodsNumber(mqEntity.getGoodsNum());
            goodsNumberLog.setLogTime(new Date());
            goodsNumberLogMapper.insert(goodsNumberLog);
//         将消息的状态改为成功
            tradeMqConsumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_SUCCESS.getCode());
            tradeMqConsumerLog.setConsumerTimestamp(new Date());
            mqConsumerLogMapper.updateById(tradeMqConsumerLog);
        }catch (Exception e){
            QueryWrapper wrapper=new QueryWrapper();
            wrapper.eq("msg_tag",tags);
            wrapper.eq("msg_keys",keys);
            wrapper.eq("group_name",groupName);
            TradeMqConsumerLog exceptionConsumerLog = mqConsumerLogMapper.selectOne(wrapper);
            if(exceptionConsumerLog==null){
                exceptionConsumerLog = new TradeMqConsumerLog();
                exceptionConsumerLog.setMsgId(msgId);
                exceptionConsumerLog.setMsgKey(keys);
                exceptionConsumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_PROCESSING.getCode());
                exceptionConsumerLog.setMsgBody(body);
                exceptionConsumerLog.setMsgTag(tags);
                exceptionConsumerLog.setConsumerTimes(0);
                mqConsumerLogMapper.insert(exceptionConsumerLog);
            }else{
                exceptionConsumerLog.setConsumerTimes(exceptionConsumerLog.getConsumerTimes()+1);
                mqConsumerLogMapper.updateById(exceptionConsumerLog);
            }
            e.printStackTrace();
        }
    }
}