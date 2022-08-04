package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.TbShopType;
import com.hmdp.mapper.TbShopTypeMapper;
import com.hmdp.service.TbShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOPType_KEY;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
@Service
public class TbShopTypeServiceImpl extends ServiceImpl<TbShopTypeMapper, TbShopType> implements TbShopTypeService {
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
            List<TbShopType> typeList=new ArrayList<>();
            for(String s:shopTypeList){
                TbShopType shopType = JSONUtil.toBean(s,TbShopType.class);
                typeList.add(shopType);
            }
            return Result.ok(typeList);
        }
//        4.不存在，则根据id去查询数据库
        List<TbShopType> typeList = query().orderByAsc("sort").list();
//        5.数据库不存在，返回错误
        if(typeList==null&&typeList.size()==0){
            return Result.fail("分类不存在");
        }
        for(TbShopType shopType:typeList){
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
