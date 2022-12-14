package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hmdp.Exception.BadRequestException;
import com.hmdp.Exception.CastException;
import com.hmdp.constant.ShopCode;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.TbUser;
import com.hmdp.entity.TbUserInfo;
import com.hmdp.entity.TradeUserMoneyLog;
import com.hmdp.mapper.TbUserMapper;
import com.hmdp.service.TbUserInfoService;
import com.hmdp.service.TbUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.TradeUserMoneyLogService;
import com.hmdp.utils.IDWorker;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
@Slf4j
@Service
public class TbUserServiceImpl extends ServiceImpl<TbUserMapper, TbUser> implements TbUserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IDWorker idWorker;
    @Resource
    private TbUserInfoService userInfoService;
    @Resource
    private TradeUserMoneyLogService tradeUserMoneyLogService;
    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1. 校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            //2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }

        //3. 符合，生成验证码
        String code = RandomUtil.randomNumbers(6);

        //4. 保存验证码到redis
        stringRedisTemplate.opsForValue().set(
                LOGIN_CODE_KEY +phone,code,
                RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        //5. 发送验证码
        log.debug("发送短信验证码成功，验证码:{}",code);

        //返回ok
        return Result.ok();
    }


    @Override
    public Result sign() {
//        获取当前登录的用户
        Long userId = UserHolder.getUser().getId();
//        获取日期
        LocalDateTime now = LocalDateTime.now();
//        拼接key
        String keySuffix=now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key=USER_SIGN_KEY+userId+keySuffix;
//        获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
//        写入redis
        stringRedisTemplate.opsForValue().setBit(key,dayOfMonth-1,true);
        return Result.ok();
    }

    @Override
    public Result signCount() {
//        获取当前登录的用户
        Long userId = UserHolder.getUser().getId();
//        获取日期
        LocalDateTime now = LocalDateTime.now();
//        拼接key
        String keySuffix=now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key=USER_SIGN_KEY+userId+keySuffix;
//        获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
//        获取本月直到今天为止所有的签到记录,返回一个十进制的数字
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth))
                        .valueAt(0)
        );
        if(result==null||result.isEmpty()){
            return Result.ok(0);
        }
        Long num = result.get(0);
        if(num==null||num==0){
            return Result.ok(0);
        }
        int count=0;
        while(true){
            if((num&1)==0){
                break;
            }else{
                count++;
            }
            num>>>=1;
        }
        return Result.ok(count);
    }

    /**
     * 登出用户
     * @param token
     * @return
     */
    @Override
    public Result logout(String token) {
        if(token==null||token==""){
            throw new BadRequestException("错误的token");
        }
        stringRedisTemplate.opsForHash().delete(LOGIN_USER_KEY+token);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {

        //1. 校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }

        //2. 校验验证码
//        从redis中获取验证码
//        Object cacheCode = session.getAttribute("code");
        Object cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY+phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.toString().equals(code)){
            //3. 不一致，报错
            return Result.fail("验证码错误");
        }
        //4.一致，根据手机号查询用户
        TbUser user = query().eq("phone", phone).one();

        //5. 判断用户是否存在
        if (user == null){
            //6. 不存在，创建新用户
            user = createUserWithPhone(phone);
        }
        TbUserInfo userInfo = userInfoService.query().eq("user_id", user.getId()).one();

        //7.保存用户信息到redis
//        session.setAttribute("user",BeanUtil.copyProperties(user,UserDTO.class));
//        随机生成token，作为登录令牌
        String token= UUID.randomUUID().toString();
//        将User对象转为Hash存储
        UserDTO userDTO= BeanUtil.copyProperties(user,UserDTO.class);
        BeanUtil.copyProperties(userInfo,userDTO);
        Map<String, Object> userDTOMap = BeanUtil.beanToMap(userDTO,new HashMap<>(),
                CopyOptions.create().setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName,fieldValue)->
                                fieldValue.toString()
                        ));
//        存储
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY+token,userDTOMap);
        stringRedisTemplate.expire(LOGIN_USER_KEY+token,LOGIN_USER_TTL, TimeUnit.MINUTES);
//        返回token
        return Result.ok(token);
    }



    private TbUser createUserWithPhone(String phone) {

        // 1.创建用户
        TbUser user = new TbUser();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        user.setUserMoney(BigDecimal.valueOf(0));
        user.setUserScore(0);
        TbUserInfo userInfo = new TbUserInfo();
        userInfo.setGender(false);
        userInfo.setCreateTime(new Date());
        userInfo.setBirthday(new Date());
        userInfo.setCity("");
        long userId = idWorker.nextId();
        userInfo.setUserId(userId);
        user.setId(userId);
        userInfo.setCredits(0);
        userInfo.setFans(0);
        userInfo.setFollowee(0);
        userInfo.setIntroduce("无个人介绍");
        userInfo.setLevel(true);
        userInfo.setUpdateTime(new Date());
        userInfoService.save(userInfo);
        // 2.保存用户
        boolean save = save(user);
        return user;
    }

    /**
     *  更新支付日志
     * @param userMoneyLog
     * @return
     */
    @Override
    public Result updateMoneyPaid(TradeUserMoneyLog userMoneyLog) {
        //1.校验参数是否合法
        if(userMoneyLog==null ||
                userMoneyLog.getUserId()==null ||
                userMoneyLog.getOrderId()==null ||
                userMoneyLog.getUseMoney()==null||
                userMoneyLog.getUseMoney().compareTo(BigDecimal.ZERO)<=0){
            CastException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        //2.查询订单余额使用日志
        QueryWrapper<TradeUserMoneyLog> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userMoneyLog.getUserId());
        wrapper.eq("order_id", userMoneyLog.getOrderId());
        long r = tradeUserMoneyLogService.count(wrapper);
        TbUser tradeUser = baseMapper.selectById(userMoneyLog.getUserId());
        //3.扣减余额...
        if(userMoneyLog.getMoneyLogType().intValue()==ShopCode.SHOP_USER_MONEY_PAID.getCode().intValue()){
            if(r>0){
                //已经付款
                CastException.cast(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY);
            }
            //减余额
            tradeUser.setUserMoney(tradeUser.getUserMoney().subtract(userMoneyLog.getUseMoney()));
            baseMapper.updateById(tradeUser);
        }
        //4.回退余额...
        if(userMoneyLog.getMoneyLogType().intValue()==ShopCode.SHOP_USER_MONEY_REFUND.getCode().intValue()){
            if(r<0){
                //如果没有支付,则不能回退余额
                CastException.cast(ShopCode.SHOP_ORDER_PAY_STATUS_NO_PAY);
            }
            //防止多次退款
            long r2 = tradeUserMoneyLogService.count(wrapper);
            if(r2>0){
                CastException.cast(ShopCode.SHOP_USER_MONEY_REFUND_ALREADY);
            }
            //退款
            tradeUser.setUserMoney(tradeUser.getUserMoney().add(userMoneyLog.getUseMoney()));
            baseMapper.updateById(tradeUser);
        }
        //5.记录订单余额使用日志
        userMoneyLog.setCreateTime(new Date());
        tradeUserMoneyLogService.save(userMoneyLog);
        return Result.ok(ShopCode.SHOP_SUCCESS.getMessage());
    }
}
