package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.dto.ScrollResult;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.TbBlog;
import com.hmdp.entity.TbFollow;
import com.hmdp.entity.TbUser;
import com.hmdp.mapper.TbBlogMapper;
import com.hmdp.service.TbBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.TbFollowService;
import com.hmdp.service.TbUserService;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.BLOG_LIKED_KEY;
import static com.hmdp.utils.RedisConstants.REED_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
@Service
public class TbBlogServiceImpl extends ServiceImpl<TbBlogMapper, TbBlog> implements TbBlogService {
    @Resource
    private TbUserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private TbFollowService followService;


    @Override
    public Result queryHotBlog(Integer current) {
        // 根据用户查询
        Page<TbBlog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<TbBlog> records = page.getRecords();
        // 查询用户
        records.forEach(blog -> {
            this.queryBlogUser(blog);
            this.isBlogLiked(blog);
        });
        return Result.ok(records);
    }

    private void queryBlogUser(TbBlog blog) {
        Long userId = blog.getUserId();
        TbUser user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

    @Override
    public Result queryBlogById(Long id) {
//        查询blog
        TbBlog blog = getById(id);

        if(blog==null){
            return Result.fail("笔记不存在");
        }
//        查询blog有关用户
        queryBlogUser(blog);
//        查询blog是否被点赞
        isBlogLiked(blog);
        return Result.ok(blog);
    }

    private void isBlogLiked(TbBlog blog) {
        UserDTO user = UserHolder.getUser();
        if(user==null){
//            用户未登陆无需查询是否点赞
            return;
        }
        String key="blog:liked:"+blog.getId();
        Long userId = user.getId();
        Double score=stringRedisTemplate.opsForZSet().score(key,userId.toString());
        blog.setIsLike(score!=null);
    }

    @Override
    public Result likeBlog(Long id) {
//        获取登录用户
        Long userId = UserHolder.getUser().getId();
//        判断当前登录用户是否已经点赞
        String key=BLOG_LIKED_KEY+id;
        Double score=stringRedisTemplate.opsForZSet().score(key,userId.toString());
        if(score==null){
//        如果未点赞，可以点赞
//        数据库点赞数+1
            boolean isSuccess = update().setSql("liked = liked +1").eq("id", id).update();
//        保存用户到Redis的set集合
            if(isSuccess){
                stringRedisTemplate.opsForZSet().add(key,userId.toString(),System.currentTimeMillis());
            }
        }else{
//        如果已点赞，取消点赞
//        数据库点赞数-1
            boolean isSuccess = update().setSql("liked = liked -1").eq("id", id).update();
//        把用户从Redis的set集合移除
            if(isSuccess){
                stringRedisTemplate.opsForZSet().remove(key,userId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result queryBlogLikes(Long id) {
        String key=BLOG_LIKED_KEY+id;
//        1.查询top5的点赞用户
        Set<String> top5=stringRedisTemplate.opsForZSet().range(key,0,4);
        if(top5==null||top5.isEmpty()){
            return Result.ok(Collections.emptyList());
        }
//        解析出其中的用户id
        List<Long> ids = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        String idStr = StrUtil.join(",", ids);
//        根据用户id查询用户
        List<UserDTO> userDTOS= userService.query().in("id",ids).last("ORDER BY FIELD(id,"+idStr+")").list()
                .stream()
                .map(user->
                        BeanUtil.copyProperties(user,UserDTO.class))
                .collect(Collectors.toList());
        return Result.ok(userDTOS);
    }

    @Override
    public Result saveBlog(TbBlog blog) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        // 保存探店博文
        save(blog);
//        保存完探店笔记之后，推送笔记id给所有粉丝
//        查询笔记作者的全部粉丝
        List<TbFollow> follows = followService.query()
                .eq("follow_user_id", user.getId())
                .list();
//        推送笔记id给所有粉丝
        for(TbFollow follow:follows){
//            获取粉丝id
            Long userId = follow.getUserId();
//            推送
            String key=REED_KEY+userId;
            stringRedisTemplate.opsForZSet().add(key,blog.getId().toString(),System.currentTimeMillis());

        }
        // 返回id
        return Result.ok(blog.getId());
    }

    @Override
    public Result queryBlogOfFollow(Long max, Integer offset) {
//        获取当前用户
        UserDTO user = UserHolder.getUser();
        Long userId = user.getId();
//        查询收件箱
        String key =REED_KEY+userId;
        Set<ZSetOperations.TypedTuple<String>> typeTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 2);
        if(typeTuples==null||typeTuples.isEmpty()){
            return Result.ok();
        }
        //        解析数据blogId，时间戳,offset
        List<Long> ids = new ArrayList<>(typeTuples.size());
        long minTime=0;
        int os=1;
        for(ZSetOperations.TypedTuple<String> tuple:typeTuples){

//            获取分数
            ids.add(Long.valueOf(tuple.getValue()));
            long time = tuple.getScore().longValue();
            if(time==minTime){
                os++;
            }else{
                minTime=time;
                os=1;
            }
        }
//        根据id查询blog
        String idStr=StrUtil.join(",",ids);
        List<TbBlog> blogs=query().in("id",ids)
                .last("ORDER BY FIELD(id,"+idStr+")")
                .list();
//        封装并且返回
        ScrollResult r=new ScrollResult();
        r.setList(blogs);
        r.setOffset(os);
        r.setMinTime(minTime);
        return Result.ok(r);
    }
}
