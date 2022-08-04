package com.hmdp.entity;

import com.baomidou.mybatisplus.annotation.IdType;

import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
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
@ApiModel(value="TbVoucher对象", description="")
public class TbVoucher implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商铺id")
    private Long shopId;

    @ApiModelProperty(value = "代金券标题")
    private String title;

    @ApiModelProperty(value = "副标题")
    private String subTitle;

    @ApiModelProperty(value = "使用规则")
    private String rules;

    @ApiModelProperty(value = "支付金额，单位是分。例如200代表2元")
    private Long payValue;

    @ApiModelProperty(value = "抵扣金额，单位是分。例如200代表2元")
    private Long actualValue;

    @ApiModelProperty(value = "0,普通券；1,秒杀券")
    private Boolean type;

    @ApiModelProperty(value = "1,上架; 2,下架; 3,过期")
    private Boolean status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /**
     * 库存
     */
    @TableField(exist = false)
    private Integer stock;

    /**
     * 生效时间
     */
    @TableField(exist = false)
    private Date beginTime;

    /**
     * 失效时间
     */
    @TableField(exist = false)
    private Date endTime;
}
