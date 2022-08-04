package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.TbShop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
public interface TbShopService extends IService<TbShop> {

    Result queryById(Long id) ;

    Result update(TbShop shop);

    Result queryShopByType(Integer typeId, Integer current, Double x, Double y);

}
