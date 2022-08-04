package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.TbBlog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
public interface TbBlogService extends IService<TbBlog> {
    Result queryHotBlog(Integer current);

    Result queryBlogById(Long id);

    Result likeBlog(Long id);

    Result queryBlogLikes(Long id);

    Result saveBlog(TbBlog blog);

    Result queryBlogOfFollow(Long max, Integer offset);

}
