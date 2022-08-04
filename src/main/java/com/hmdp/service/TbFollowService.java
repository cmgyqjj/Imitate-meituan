package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.TbFollow;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
public interface TbFollowService extends IService<TbFollow> {
    Result isFollow(Long followUserId);

    Result follow(Long followUserId, Boolean isFollow);

    Result followCommons(Long id);

}
