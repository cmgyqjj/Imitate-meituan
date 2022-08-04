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
@ApiModel(value="TbUserInfo对象", description="")
public class TbUserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键，用户id")
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    @ApiModelProperty(value = "城市名称")
    private String city;

    @ApiModelProperty(value = "个人介绍，不要超过128个字符")
    private String introduce;

    @ApiModelProperty(value = "粉丝数量")
    private Integer fans;

    @ApiModelProperty(value = "关注的人的数量")
    private Integer followee;

    @ApiModelProperty(value = "性别，0：男，1：女")
    private Boolean gender;

    @ApiModelProperty(value = "生日")
    private Date birthday;

    @ApiModelProperty(value = "积分")
    private Integer credits;

    @ApiModelProperty(value = "会员级别，0~9级,0代表未开通会员")
    private Boolean level;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;


}
