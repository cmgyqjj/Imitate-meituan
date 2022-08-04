package com.hmdp.mapper;

import com.hmdp.entity.TbVoucher;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
public interface TbVoucherMapper extends BaseMapper<TbVoucher> {
    List<TbVoucher> queryVoucherOfShop(@Param("shopId") Long shopId);
}
