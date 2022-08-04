package com.hmdp.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.hmdp.entity.TbBlog;
import com.hmdp.entity.TbShop;
import com.hmdp.service.TbBlogService;
import com.hmdp.service.TbShopService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_BLOG_KEY;
import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;

/**
 * @author:{QJJ}
 * @date:{2022}
 * @description:对全部缓存预热
 **/
@Configuration
public class RedisHandler implements InitializingBean {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private TbShopService shopService;

    @Resource
    private TbBlogService blogService;

    private static final ObjectMapper mapper=new ObjectMapper();

    @Override
    public void afterPropertiesSet() throws Exception {
//        初始化缓存
//        查询shop和blog信息
//        TODO 这里实现的是对全部数据进行缓存，而真实场景下，数据较多
//         可能只会对高赞高访问商家和博客进行缓存
        List<TbShop> shops = shopService.list();
        List<TbBlog> blogs = blogService.list();
//        放入缓存
        for(TbShop shop:shops){
//            把shop序列化为Json
            String json = mapper.writeValueAsString(shop);
//            存入redis
            stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY+shop.getId(),json);
        }
//        放入缓存
        for(TbBlog blog:blogs){
//            把shop序列化为Json
            String json = mapper.writeValueAsString(blog);
//            存入redis
            stringRedisTemplate.opsForValue().set(CACHE_BLOG_KEY+blog.getId(),json);
        }

    }
}
