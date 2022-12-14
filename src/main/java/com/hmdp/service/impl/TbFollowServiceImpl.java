package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.TbFollow;
import com.hmdp.mapper.TbFollowMapper;
import com.hmdp.service.TbFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.TbUserService;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
@Service
public class TbFollowServiceImpl extends ServiceImpl<TbFollowMapper, TbFollow> implements TbFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private TbUserService userService;

    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
//        获取登录用户
        Long userId = UserHolder.getUser().getId();
        String key="follows:"+userId;
        if(isFollow){
//            关注，新增数据
            TbFollow follow=new TbFollow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            boolean isSuccess = save(follow);
            if(isSuccess){
//                把关注用户的id，放入redis的set集合
                stringRedisTemplate.opsForSet().add(key,followUserId.toString());
            }
        }else{
            remove(new QueryWrapper<TbFollow>()
                    .eq("user_id",userId)
                    .eq("follow_user_id",followUserId));
            stringRedisTemplate.opsForSet().remove(key,followUserId.toString());
        }
        return Result.ok();
    }

    @Override
    public Result followCommons(Long id) {
        Long userId = UserHolder.getUser().getId();
        String key="follows:"+userId;
        String key2="follows:"+id;
        Set<String> intersect=stringRedisTemplate.opsForSet().intersect(key,key2);
        List<Long> ids = intersect.stream().map(Long::valueOf)
                .collect(Collectors.toList());
        if(intersect==null||intersect.isEmpty()){
            return Result.ok(Collections.emptyList());
        }
        List<UserDTO> users = userService.listByIds(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return Result.ok(users);
    }

    @Override
    public Result isFollow(Long followUserId) {
        Long userId = UserHolder.getUser().getId();
//        查询是否关注
        Integer count = query().eq("user_id", userId)
                .eq("follow_user_id", followUserId)
                .count();
        if(count>0){
            return Result.ok(true);
        }else{
            return Result.ok(false);
        }
    }



}
