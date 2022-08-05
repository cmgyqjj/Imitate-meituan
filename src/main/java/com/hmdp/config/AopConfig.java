package com.hmdp.config;

import cn.hutool.core.date.DateUtil;
import com.hmdp.dto.Result;
import com.hmdp.utils.IDWorker;
import com.hmdp.utils.UserHolder;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author:{QJJ}
 * @date:{2022}
 * @description:
 **/

/**
 * 测试切面
 */
@Aspect
@Component
public class AopConfig {

    @Resource
    private RedissonClient redissonClient;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }
    /**
     * 指定切面的切入点
     */
    @Pointcut("@annotation(com.hmdp.aop.TestAnnotation)")
    public void pointcutAnnotation() {
    }
    /**
     * 环绕通知，围绕着方法执行
     * 环绕通知需要携带ProceedingJoinPoint类型的参数
     * 环绕通知类似于动态代理的全过程：ProceedingJoinPoint类型的参数可以决定是否执行目标方法。
     * 而且环绕通知必须有返回值，返回值即为目标方法的返回值
     */
    @Around(value = "pointcutAnnotation()")
    public Object methodAround(ProceedingJoinPoint proceedingJoinPoint) {
        Object result = null;
        String methodName = proceedingJoinPoint.getSignature().getName();
        Object[] args = proceedingJoinPoint.getArgs();
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        // 获取请求的方法参数名称
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        String[] paramNames = u.getParameterNames(method);
        String params = "";
        if (args != null && paramNames != null) {
            for (int i = 0; i < args.length; i++) {
                params += "  " + paramNames[i] + ": " + args[i];
            }
        }
        Long userId = UserHolder.getUser().getId();
        RLock lock = redissonClient.getLock(userId+methodName+params);
        boolean isLock = lock.tryLock();
        if(!isLock){
            return Result.ok();
        }
        //执行目标方法
        try {
            result = proceedingJoinPoint.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        lock.unlock();
        return result;
    }

}
