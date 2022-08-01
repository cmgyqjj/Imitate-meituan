package com.hmdp.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hmdp.entity.Blog;
import com.hmdp.entity.Shop;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IShopService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static io.reactivex.internal.util.NotificationLite.getValue;

/**
 * @author:{QJJ}
 * @date:{2022}
 * @description:
 **/
@Configuration
public class CaffeineConfig {

    @Resource
    private IShopService shopService;

    @Resource
    private IBlogService blogService;

    @Bean
    public Cache<Long, Shop> shopCache(){
//        设置一个初试100，最大10000容量,超过则替换，并且单个缓存有效时间为10分钟的Caffeine
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(10000)
                .expireAfterAccess(10L, TimeUnit.MINUTES)
                //指定刷新策略
                .refreshAfterWrite(5, TimeUnit.MINUTES)
                // 弱引用key
                .weakKeys()
                // 弱引用value
                .weakValues()
                .build(new CacheLoader<Long, Shop>() {
                    @Override
                    public Shop load(Long id) {
                        // 这里我们就可以从数据库或者其他地方查询最新的数据
//                        这里我使用数据库导入
                        return (Shop) shopService.queryById(id).getData();
                    }
                });
    }

    @Bean
    public Cache<Long, Blog> blogCache(){
//        设置一个初试100，最大10000容量,超过则替换，并且单个缓存有效时间为10分钟的Caffeine
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(10000)
                .expireAfterAccess(10L, TimeUnit.MINUTES)
                //指定刷新策略
                .refreshAfterWrite(5, TimeUnit.MINUTES)
                // 弱引用key
                .weakKeys()
                // 弱引用value
                .weakValues()
                .build(new CacheLoader<Long, Blog>() {
                    @Override
                    public Blog load(Long id) {
                        // 这里我们就可以从数据库或者其他地方查询最新的数据
                        //                        这里我使用数据库导入
                        return (Blog) blogService.queryBlogById(id).getData();
                    }
                });
    }

}
