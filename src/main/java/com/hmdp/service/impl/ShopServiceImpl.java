package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.hmdp.Exception.BadRequestException;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisData;
import com.hmdp.utils.SystemConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 * @author:{QJJ}
 * @date:{2022}
 * @description:
 **/
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final ExecutorService CAChe_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);


    @Override
    public Result queryById(Long id) {
//        缓存穿透
//        Shop shop=queryWithPassThrough(id);
//        互斥锁解决缓存击穿
//        利用进程缓存缓存shop
        Shop shop = queryWithPassMutex(id);
        if(shop==null){
            return Result.fail("店铺不存在");
        }
//        返回
        return Result.ok(shop);
    }


    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }


//    设置逻辑过期时间，而非真实过期时间
    private void saveShop2Redis(Long id,Long expireSeconds){
//        查询店铺数据
//        利用进程缓存缓存shop
        Shop shop = getById(id);
//        封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
//        写入redis
        stringRedisTemplate.opsForValue().set(
                CACHE_SHOP_KEY+id,JSONUtil.toJsonStr(redisData));
    }
//  逻辑过期解决缓存击穿
    public Shop queryWithLogicalExpire(Long id) {
        //        1.从redis中查询商铺缓存
        String key = CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
    //        2.判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            return null;
        }
    //    命中，需要先把json反序列化成对象
        RedisData redisData=JSONUtil.toBean(shopJson,RedisData.class);
        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(),Shop.class);
        //    判断是否过期
        LocalDateTime expireTime =redisData.getExpireTime();
        if(expireTime.isAfter(LocalDateTime.now())){
    //    未过期，直接返回店铺信息
            return shop;
        }
    //    已过期，需要缓存重建
    //    缓存重建
    //    获取互斥锁
        String lockKey=LOCK_SHOP_KEY+id;
    //    判断是否获取锁成功
        if(tryLock(lockKey)){
    //        做DoubleCheck
            expireTime =redisData.getExpireTime();
            if(expireTime.isAfter(LocalDateTime.now())){
    //    未过期，直接返回店铺信息
                return shop;
            }
    //    成功，开启独立线程，实现缓存重建
            try {
                CAChe_REBUILD_EXECUTOR.submit(()->{
                    this.saveShop2Redis(id, 20L);
                });
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                //        释放锁
                unlock(lockKey);
            }
        }
    //    失败，直接返回过期的商品信息
        return shop;
    }




    //对reds进行查询，如果查不到则互斥的访问数据库
    public Shop queryWithPassMutex(Long id) {
//        1.从redis中查询商铺缓存
        String key = CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
//        2.判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
//        3.存在，直接返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
//        判断命中是否是空值
        if (shopJson != null) {
            return null;
        }
        Shop shop=null;
        try {
//        实现缓存重建
//        获取互斥锁
            String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
            boolean isLock = tryLock(lockKey);
//        判断是否获取成功
            while (!isLock) {
//        失败，则休眠并重试

                Thread.sleep(50);

                isLock = tryLock(lockKey);
            }
//        获取锁成功，二次查询缓存，判断这个时候是否其他线程已经从数据库缓存到redis中了
            shopJson = stringRedisTemplate.opsForValue().get(key);
//        2.判断是否存在
            if (StrUtil.isNotBlank(shopJson)) {
                //        3.存在，直接返回
                shop = JSONUtil.toBean(shopJson, Shop.class);
                return shop;
            }
//        判断命中是否是空值
            if (shopJson != null) {
                return null;
            }
//        获取锁成功后，依然没有从缓存中找到，根据id查询数据库
            shop = getById(id);
//        5.数据库不存在，返回错误
            if (shop == null) {
//            将空值写入redis
//            这里使用的缓存null值的方式，后续也可以使用布隆过滤器的方式来处理
//            也可以增加id的复杂度，避免被猜测id规律
                stringRedisTemplate.opsForValue().set(key, "", CACHE_SHOP_TTL, TimeUnit.MINUTES);
                return null;
            }
//        6.存在写入redis
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            unlock(LOCK_SHOP_KEY + id);
        }
//        7.返回
        return shop;
    }

    public Result queryWithPassThrough(Long id) {
        //        1.从redis中查询商铺缓存
        String key = CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
//        2.判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            //        3.存在，直接返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }
//        判断命中是否是空值
        if (shopJson != null) {
            return null;
        }
//        4.不存在，则根据id去查询数据库
        Shop shop = getById(id);
//        5.数据库不存在，返回错误
        if (shop == null) {
//            为空值缓存
//            这里使用的缓存null值的方式，后续也可以使用布隆过滤器的方式来处理
            stringRedisTemplate.opsForValue().set(key, "", CACHE_SHOP_TTL, TimeUnit.MINUTES);
            return Result.fail("店铺不存在");
        }
//        6.存在写入redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);

//        释放锁
        unlock(LOCK_SHOP_KEY + id);
//        7.返回
        return Result.ok(shop);
    }

    @Override
    @Transactional
    public Result update(Shop shop) {

        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空");
        }
//        更新数据
        updateById(shop);
//        删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        return Result.ok();
    }

    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
//        是否需要根据坐标查询
        if(x==null||y==null){
            // 根据类型分页查询
            Page<Shop> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            // 返回数据
            return Result.ok(page.getRecords());
        }
//        计算分页参数
        int from =(current-1)*SystemConstants.DEFAULT_PAGE_SIZE;
        int end=current*SystemConstants.DEFAULT_PAGE_SIZE;
        //        查询redis，按照距离排序，分页
        String key = SHOP_GEO_KEY+typeId;
//        TODO 这个距离可以从前端传进来
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo()
                .search(
                        key,
                        GeoReference.fromCoordinate(x, y),
                        new Distance(5000),
                        RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
                );
//        解析出id
        if(results==null){
            return Result.ok(Collections.emptyList());
        }
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
//        截取from-end的部分
        List<Long> ids = new ArrayList<>(list.size());
        Map<String,Distance> distanceMap=new HashMap<>(list.size());
        if(list.size()<=from){
            return Result.ok(Collections.emptyList());
        }
        list.stream().skip(from).forEach(result->{
            String shopIdStr=result.getContent().getName();
            ids.add(Long.valueOf(shopIdStr));
//            获取距离
            Distance distance = result.getDistance();
            distanceMap.put(shopIdStr,distance);
        });
//        根据id查询Shop
        String idStr=StrUtil.join(",",ids);
        List<Shop> shops;
        shops = query().in("id",ids).last("ORDER By FIELD(id,"+idStr+")").list();
        for(Shop shop :shops){
            shop.setDistance(distanceMap.get(shop.getId().toString()).getValue());
        }
        return Result.ok(shops);
    }
}
