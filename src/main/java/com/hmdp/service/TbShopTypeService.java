package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.TbShopType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
public interface TbShopTypeService extends IService<TbShopType> {

    Result queryList();
}
