package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.entity.TradeOrder;
import com.hmdp.service.TradeOrderService;
import org.springframework.web.bind.annotation.RequestBody;
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
@RequestMapping("/order")
public class TradeOrderController {

    @Resource
    private TradeOrderService orderService;

    @RequestMapping("/confirm")
    public Result confirmOrder(@RequestBody TradeOrder order){
        return orderService.confirmOrder(order);
    }

}

