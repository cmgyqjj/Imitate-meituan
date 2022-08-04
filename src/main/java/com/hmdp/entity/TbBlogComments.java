package com.hmdp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
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
@ApiModel(value="TbBlogComments对象", description="")
public class TbBlogComments implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "探店id")
    private Long blogId;

    @ApiModelProperty(value = "关联的1级评论id，如果是一级评论，则值为0")
    private Long parentId;

    @ApiModelProperty(value = "回复的评论id")
    private Long answerId;

    @ApiModelProperty(value = "回复的内容")
    private String content;

    @ApiModelProperty(value = "点赞数")
    private Integer liked;

    @ApiModelProperty(value = "状态，0：正常，1：被举报，2：禁止查看")
    private Boolean status;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;


}
