package com.hmdp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * @author:{QJJ}
 * @date:{2022}
 * @description:
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("trade_mq_consumer_log")
public class TradeMqConsumerLog implements Serializable {

    private String msgId;
    private String groupName;
    private String msgTag;
    private String msgKey;
    private String msgBody;
    private Integer consumerStatus;
    private Integer consumerTimes;
    private Date consumerTimestamp;
    private String remark;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getMsgTag() {
        return msgTag;
    }

    public void setMsgTag(String msgTag) {
        this.msgTag = msgTag;
    }

    public String getMsgKey() {
        return msgKey;
    }

    public void setMsgKey(String msgKey) {
        this.msgKey = msgKey;
    }

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public Integer getConsumerStatus() {
        return consumerStatus;
    }

    public void setConsumerStatus(Integer consumerStatus) {
        this.consumerStatus = consumerStatus;
    }

    public Integer getConsumerTimes() {
        return consumerTimes;
    }

    public void setConsumerTimes(Integer consumerTimes) {
        this.consumerTimes = consumerTimes;
    }

    public Date getConsumerTimestamp() {
        return consumerTimestamp;
    }

    public void setConsumerTimestamp(Date consumerTimestamp) {
        this.consumerTimestamp = consumerTimestamp;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}



