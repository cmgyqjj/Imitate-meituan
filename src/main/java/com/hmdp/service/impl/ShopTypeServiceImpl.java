package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author:{QJJ}
 * @date:{2022}
 * @description:
 **/
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryList() {
//        1.从redis中查询商铺缓存
        String key=CACHE_SHOPType_KEY;
        List<String> shopTypeList = new ArrayList<>();
        shopTypeList=stringRedisTemplate.opsForList().range(key,0,-1);
//        2.判断是否缓存命中
//        3.中了返回
        if(!shopTypeList.isEmpty()){
            List<ShopType> typeList=new ArrayList<>();
            for(String s:shopTypeList){
                ShopType shopType = JSONUtil.toBean(s,ShopType.class);
                typeList.add(shopType);
            }
            return Result.ok(typeList);
        }
//        4.不存在，则根据id去查询数据库
        List<ShopType> typeList = query().orderByAsc("sort").list();
//        5.数据库不存在，返回错误
        if(typeList==null&&typeList.size()==0){
            return Result.fail("分类不存在");
        }
        for(ShopType shopType:typeList){
            String s=JSONUtil.toJsonStr(shopType);
            shopTypeList.add(s);
        }
//        6.存在写入redis
        stringRedisTemplate.opsForList().rightPushAll(key,shopTypeList);
        stringRedisTemplate.expire(key,LOGIN_USER_TTL, TimeUnit.MINUTES);
//        7.返回
        return Result.ok(typeList);
    }
}
