package com.hmdp;

/**
 * @author:{QJJ}
 * @date:{2022}
 * @description:
 **/


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CaffeineTest {
    @Test
    public void test1() {
        Cache<String, String> cache = Caffeine.newBuilder()
                // 数量上限
                .maximumSize(1024)
                // 表示自从最后一次写入后多久就会过期
                .expireAfterWrite(5, TimeUnit.MINUTES)
                //表示自从最后一次访问（写入或者读取）后多久就会过期；
                .expireAfterAccess(5, TimeUnit.MINUTES)
                //自定义过期策略
                //.expireAfter()
                //指定刷新策略
                .refreshAfterWrite(5, TimeUnit.MINUTES)
                // 弱引用key
                .weakKeys()
                // 弱引用value
                .weakValues()
                //记录下缓存的一些统计数据，例如命中率等
                .recordStats()
                // 剔除监听
                .removalListener((RemovalListener<String, String>) (key, value, cause) ->
                        System.out.println("key:" + key + ", value:" + value + ", 删除原因:" + cause.toString()))

                //刷新策略
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String k) {
                        // 这里我们就可以从数据库或者其他地方查询最新的数据
                        return getValue(k);
                    }
                });
// 将数据放入本地缓存中
        cache.put("username", "afei");
        cache.put("password", "123456");
// 从本地缓存中取出数据
        System.out.println(cache.getIfPresent("username"));
        System.out.println(cache.getIfPresent("password"));
        System.out.println(cache.get("blog", key -> {
            // 本地缓存没有的话，从数据库或者Redis中获取
            return getValue(key);
        }));
        //失效
        cache.invalidate("username");
        cache.invalidateAll();
        cache.invalidateAll(new ArrayList<>());
    }


    /**结果
     * afei
     * 123456
     * 111
     * key:username, value:afei, 删除原因:EXPLICIT
     * key:blog, value:111, 删除原因:EXPLICIT
     * key:password, value:123456, 删除原因:EXPLICIT
     *
     * */
    private String getValue(String key) {
        return "111";
    }



    @Test
    public void test2() {
        Cache<String, String> cache = Caffeine.newBuilder()
                // 数量上限
                .maximumSize(2)
                // 表示自从最后一次写入后多久就会过期
                .expireAfterWrite(5, TimeUnit.MINUTES)
                //表示自从最后一次访问（写入或者读取）后多久就会过期；
                .recordStats()
                // 剔除监听
                .removalListener((RemovalListener<String, String>) (key, value, cause) ->
                        System.out.println("key:" + key + ", value:" + value + ", 删除原因:" + cause.toString()))

                //刷新策略
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String k) {
                        // 这里我们就可以从数据库或者其他地方查询最新的数据
                        return getValue(k);
                    }
                });
// 将数据放入本地缓存中
        cache.put("username", "afei");
        cache.put("password", "123456");
        cache.put("pas", "123456");
        cache.put("1","3");
        cache.invalidate("1");
/**结果
 * key:username, value:afei, 删除原因:SIZE
 * key:1, value:3, 删除原因:EXPLICIT
 * key:password, value:123456, 删除原因:SIZE
 * */

    }


}
/**这个是别人的总结
 *剔除算法方面，GuavaCache采用的是「LRU」算法，而Caffeine采用的是「Window TinyLFU」算法，这是两者之间最大，也是根本的区别。
 *
 * 立即失效方面，Guava会把立即失效 (例如：expireAfterAccess(0) and expireAfterWrite(0)) 转成设置最大Size为0。这就会导致剔除提醒的原因是SIZE而不是EXPIRED。Caffiene能正确识别这种剔除原因。
 *
 * 取代提醒方面，Guava只要数据被替换，不管什么原因，都会触发剔除监听器。而Caffiene在取代值和先前值的引用完全一样时不会触发监听器。
 *
 * 异步化方方面，Caffiene的很多工作都是交给线程池去做的（默认：ForkJoinPool.commonPool()），例如：剔除监听器，刷新机制，维护工作等。
 *
 *
 * */



