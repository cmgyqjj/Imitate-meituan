package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.TbSeckillVoucher;
import com.hmdp.entity.TbVoucher;
import com.hmdp.mapper.TbVoucherMapper;
import com.hmdp.service.TbSeckillVoucherService;
import com.hmdp.service.TbVoucherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static com.hmdp.utils.RedisConstants.SECKILL_STOCK_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
@Service
public class TbVoucherServiceImpl extends ServiceImpl<TbVoucherMapper, TbVoucher> implements TbVoucherService {

    @Resource
    private TbSeckillVoucherService seckillVoucherService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        List<TbVoucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        // 返回结果
        return Result.ok(vouchers);
    }

    @Override
    @Transactional
    public void addSeckillVoucher(TbVoucher voucher) {
        // 保存优惠券
        save(voucher);
        // 保存秒杀信息
        TbSeckillVoucher seckillVoucher = new TbSeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
        // 保存秒杀库存到Redis中
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + voucher.getId(), voucher.getStock().toString());
    }
}
