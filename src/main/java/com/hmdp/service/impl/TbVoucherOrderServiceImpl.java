package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.TbVoucherOrder;
import com.hmdp.mapper.TbVoucherOrderMapper;
import com.hmdp.service.TbSeckillVoucherService;
import com.hmdp.service.TbVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
@Service
public class TbVoucherOrderServiceImpl extends ServiceImpl<TbVoucherOrderMapper, TbVoucherOrder> implements TbVoucherOrderService {

    @Resource
    private TbSeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private BlockingQueue<TbVoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024*1024);
    private static final ExecutorService SECKILL_ORDER_EXECUTOR= Executors.newSingleThreadScheduledExecutor();

    //    这个注解表示,在类初始化完毕之后就执行
    @PostConstruct
    private void init(){
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    private class VoucherOrderHandler implements Runnable{
        @Override
        public void run() {
            while(true){
//                获取队列中的订单信息
                try {
                    TbVoucherOrder voucherOrder=orderTasks.take();
//                    创建订单
                    handlerVoucherOrder(voucherOrder);
                } catch (InterruptedException e) {
                    log.error("处理订单异常",e);
                }
            }
        }
    }

    private void handlerVoucherOrder(TbVoucherOrder voucherOrder) {
        RLock lock = redissonClient.getLock("lock:order:" + voucherOrder.getUserId());
        boolean isLock = lock.tryLock();
//        获取锁成功
        if(!isLock){
//            获取锁失败,返回错误
//            理论下不太可能，因为redis已经做了判断
            log.error("不允许重复下单");
            return;
        }
        try {
            proxy.createVoucheOrder(voucherOrder);
        }finally {
//            释放锁
            lock.unlock();
        }
    }

    private TbVoucherOrderService proxy;

    @Override
    public Result seckillVoucher(Long voucherId) {
//        获取用户id
        Long userId = UserHolder.getUser().getId();
//        执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString()
        );
//        判断结果是否为0
        if(result.intValue()!=0){
            if(result.intValue()==1){
                return Result.fail("库存不足");
            }else if(result.intValue()==2){
                return Result.fail("不能重复下单");
            }
        }
//        如果是0,说明有购买资格
        long orderId = redisIdWorker.nextId("order");
//        订单id
        TbVoucherOrder voucherOrder = new TbVoucherOrder();
        voucherOrder.setId(orderId);
//        用户id
        voucherOrder.setUserId(userId);
//        代金券id
        voucherOrder.setVoucherId(voucherId);
//        创建一个阻塞队列
        orderTasks.add(voucherOrder);
//        TODO 使用mq作为消息队列更好
//        获取代理对象
        proxy  = (TbVoucherOrderService) AopContext.currentProxy();
//        返回订单id
        return Result.ok(orderId);
    }


//    @Override
//    public Result seckillVoucher(Long voucherId) {
////        查询优惠卷星系
//        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
////        判断秒杀是否开始
//        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
//            return Result.fail("秒杀尚未开始");
//        }
//        //        如果秒杀已经结束，返回异常
//        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
//            return Result.fail("秒杀已经结束");
//        }
////        如果秒杀已经开始，判断库存是否充足
//        if (voucher.getStock() < 1) {
//            return Result.fail("库存不足");
//        }
//
//        Long userId = UserHolder.getUser().getId();
////        创建锁对象
////        SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
//        RLock lock = redissonClient.getLock("lock:order:" + userId);
//        boolean isLock = lock.tryLock();
////        获取锁成功
//        if(!isLock){
////            获取锁失败,返回错误
//            return Result.fail("不允许重复下单");
//        }
//        try {
//            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
//            return proxy.createVouche(voucherId);
//        }finally {
////            释放锁
//            lock.unlock();
//        }
//    }

    @Transactional
    public void createVoucheOrder(TbVoucherOrder voucherOrder) {
//    用户id
        Long userId = UserHolder.getUser().getId();
        //    一人一单
        Integer count = query().eq("user_id", userId)
                .eq("voucher_id", voucherOrder.getVoucherId())
                .count();
        if (count > 0) {
//            用户已经购买过了
            log.error("用户已经购买过一次了");
            return;
        }
//    库存充足，扣减库存
//        这里使用了一个乐观锁的思路解决超卖问题
        boolean success = seckillVoucherService.update()
                .setSql("stock =stock-1")
                .eq("voucher_id", voucherOrder.getVoucherId())
                .gt("stock", 0)
                .update();
        if (!success) {
            log.error("库存不足");
            return;
        }
//        创建订单
        save(voucherOrder);
    }
}
