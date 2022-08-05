package com.hmdp.config;

import cn.hutool.core.date.DateUtil;
import com.hmdp.utils.IDWorker;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IDWorker idWorker;
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
        stringRedisTemplate.opsForValue().set(methodName+idWorker.nextId(),"1");
        System.out.println("[" + DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss")
                + "]环绕通知：方法名：" + methodName + "，参数" + Arrays.asList(proceedingJoinPoint.getArgs()));
        //执行目标方法
        try {
            result = proceedingJoinPoint.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
