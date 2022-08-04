package com.hmdp.service.impl;

import com.hmdp.entity.TbSeckillVoucher;
import com.hmdp.mapper.TbSeckillVoucherMapper;
import com.hmdp.service.TbSeckillVoucherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务实现类
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
@Service
public class TbSeckillVoucherServiceImpl extends ServiceImpl<TbSeckillVoucherMapper, TbSeckillVoucher> implements TbSeckillVoucherService {

}
