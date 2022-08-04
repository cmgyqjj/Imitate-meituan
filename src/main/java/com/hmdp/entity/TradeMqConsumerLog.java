package com.hmdp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TradeMqConsumerLog对象", description="")
public class TradeMqConsumerLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private String msgId;

    @TableId(value = "group_name", type = IdType.AUTO)
    private String groupName;

    private String msgTag;

    private String msgKey;

    private String msgBody;

    @ApiModelProperty(value = "0:正在处理;1:处理成功;2:处理失败")
    private Integer consumerStatus;

    private Integer consumerTimes;

    private Date consumerTimestamp;

    private String remark;


}
