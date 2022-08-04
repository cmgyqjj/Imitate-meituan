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
 * 秒杀优惠券表，与优惠券是一对一关系
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="TbSeckillVoucher对象", description="秒杀优惠券表，与优惠券是一对一关系")
public class TbSeckillVoucher implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "关联的优惠券的id")
    @TableId(value = "voucher_id", type = IdType.AUTO)
    private Long voucherId;

    @ApiModelProperty(value = "库存")
    private Integer stock;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "生效时间")
    private Date beginTime;

    @ApiModelProperty(value = "失效时间")
    private Date endTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;


}
