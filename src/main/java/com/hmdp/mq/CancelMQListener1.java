package com.hmdp.mq;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hmdp.constant.ShopCode;
import com.hmdp.entity.MQEntity;
import com.hmdp.entity.TradeGoods;
import com.hmdp.entity.TradeGoodsNumberLog;
import com.hmdp.entity.TradeMqConsumerLog;
import com.hmdp.mapper.TradeGoodsMapper;
import com.hmdp.mapper.TradeMqConsumerLogMapper;
import com.hmdp.service.IGoodsService;
import com.hmdp.service.ITradeGoodsNumberLogService;
import com.hmdp.service.ITradeMqConsumerLogService;
import com.hmdp.service.ITradeMqProducerTempService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author:{QJJ}
 * @date:{2022}
 * @description:
 **/
@Slf4j
@Component
@RocketMQMessageListener(topic = "${mq.order.topic}",consumerGroup = "${mq.order.consumer.group.name}",messageModel = MessageModel.BROADCASTING )
public class CancelMQListener1 implements RocketMQListener<MessageExt> {


    @Value("${mq.order.consumer.group.name}")
    private String groupName;

    @Resource
    private ITradeGoodsNumberLogService tradeGoodsNumberLogService;
    @Resource
    private IGoodsService goodsService;

    @Resource
    private ITradeMqConsumerLogService tradeMqConsumerLogService;

    @Resource
    private ITradeMqProducerTempService tradeMqProducerTempService;

    @Override
    public void onMessage(MessageExt messageExt) {
        String msgId=null;
        String tags=null;
        String keys=null;
        String body=null;
        try {
            //1. 解析消息内容
            msgId = messageExt.getMsgId();
            tags= messageExt.getTags();
            keys= messageExt.getKeys();
            body= new String(messageExt.getBody(),"UTF-8");

            log.info("接受消息成功");

            //2. 查询消息消费记录
            TradeMqConsumerLog primaryKey = new TradeMqConsumerLog();
            primaryKey.setMsgTag(tags);
            primaryKey.setMsgKey(keys);
            primaryKey.setGroupName(groupName);
            TradeMqConsumerLog mqConsumerLog = tradeMqConsumerLogService.query().eq("msg_tag",tags).eq("msg_key",keys).eq("group_name",groupName).one();

            if(mqConsumerLog!=null){
                //3. 判断如果消费过...
                //3.1 获得消息处理状态
                Integer status = mqConsumerLog.getConsumerStatus();
                //处理过...返回
                if(ShopCode.SHOP_MQ_MESSAGE_STATUS_SUCCESS.getCode().intValue()==status.intValue()){
                    log.info("消息:"+msgId+",已经处理过");
                    return;
                }

                //正在处理...返回
                if(ShopCode.SHOP_MQ_MESSAGE_STATUS_PROCESSING.getCode().intValue()==status.intValue()){
                    log.info("消息:"+msgId+",正在处理");
                    return;
                }

                //处理失败
                if(ShopCode.SHOP_MQ_MESSAGE_STATUS_FAIL.getCode().intValue()==status.intValue()){
                    //获得消息处理次数
                    Integer times = mqConsumerLog.getConsumerTimes();
                    if(times>3){
                        log.info("消息:"+msgId+",消息处理超过3次,不能再进行处理了");
                        return;
                    }
                    mqConsumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_PROCESSING.getCode());

                    //使用数据库乐观锁更新
                    boolean r = tradeMqConsumerLogService.update(mqConsumerLog,new QueryWrapper<TradeMqConsumerLog>()
                            .eq("msg_tag",mqConsumerLog.getMsgTag())
                            .eq("msg_key",mqConsumerLog.getMsgKey())
                            .eq("group_name",groupName)
                            .eq("consumer_times",mqConsumerLog.getConsumerTimes()));
                    if(r!=true){
                        //未修改成功,其他线程并发修改
                        log.info("并发修改,稍后处理");
                    }
                }

            }else{
                //4. 判断如果没有消费过...
                mqConsumerLog = new TradeMqConsumerLog();
                mqConsumerLog.setMsgTag(tags);
                mqConsumerLog.setMsgKey(keys);
                mqConsumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_PROCESSING.getCode());
                mqConsumerLog.setMsgBody(body);
                mqConsumerLog.setMsgId(msgId);
                mqConsumerLog.setConsumerTimes(0);
                //将消息处理信息添加到数据库
                tradeMqConsumerLogService.save(mqConsumerLog);
            }
            //5. 回退库存
            MQEntity mqEntity = JSON.parseObject(body, MQEntity.class);
            Long goodsId = mqEntity.getGoodsId();
            TradeGoods goods = goodsService.getById(goodsId);
            goods.setGoodsNumber(goods.getGoodsNumber()+mqEntity.getGoodsNum());
            goodsService.updateById(goods);
            //记录库存操作日志
            TradeGoodsNumberLog goodsNumberLog = new TradeGoodsNumberLog();
            goodsNumberLog.setOrderId(mqEntity.getOrderId());
            goodsNumberLog.setGoodsId(goodsId);
            goodsNumberLog.setGoodsNumber(mqEntity.getGoodsNum());
            goodsNumberLog.setLogTime(new Date());
            tradeGoodsNumberLogService.save(goodsNumberLog);

            //6. 将消息的处理状态改为成功
            mqConsumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_SUCCESS.getCode());
            mqConsumerLog.setConsumerTimestamp((java.sql.Date) new Date());
            tradeMqConsumerLogService.updateById(mqConsumerLog);
            log.info("回退库存成功");
        } catch (Exception e) {
            e.printStackTrace();
            TradeMqConsumerLog primaryKey = new TradeMqConsumerLog();
            primaryKey.setMsgTag(tags);
            primaryKey.setMsgKey(keys);
            primaryKey.setGroupName(groupName);
            TradeMqConsumerLog mqConsumerLog = tradeMqConsumerLogService.query().eq("msg_tag",tags).eq("msg_key",keys).eq("group_name",groupName).one();
            if(mqConsumerLog==null){
                //数据库未有记录
                mqConsumerLog = new TradeMqConsumerLog();
                mqConsumerLog.setMsgTag(tags);
                mqConsumerLog.setMsgKey(keys);
                mqConsumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_FAIL.getCode());
                mqConsumerLog.setMsgBody(body);
                mqConsumerLog.setMsgId(msgId);
                mqConsumerLog.setConsumerTimes(1);
                tradeMqConsumerLogService.save(mqConsumerLog);
            }else{
                mqConsumerLog.setConsumerTimes(mqConsumerLog.getConsumerTimes()+1);
                tradeMqConsumerLogService.updateById(mqConsumerLog);
            }
        }

    }
}
