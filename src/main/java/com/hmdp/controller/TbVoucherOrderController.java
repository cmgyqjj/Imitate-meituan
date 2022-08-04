package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.service.TbVoucherOrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
@RestController
@RequestMapping("/voucher-order")
public class TbVoucherOrderController {

    @Resource
    private TbVoucherOrderService voucherOrderService;


    @PostMapping("seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        return voucherOrderService.seckillVoucher(voucherId);
    }
}

