package com.hmdp.controller;

import com.hmdp.dto.Result;
import com.hmdp.entity.TradeOrder;
import com.hmdp.service.IOrderService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author:{QJJ}
 * @date:{2022}
 * @description:
 **/
@RestController
@RequestMapping("/order")
public class OrderControllre {

    @Resource
    private IOrderService orderService;

    @RequestMapping("/confirm")
    public Result confirmOrder(@RequestBody TradeOrder order){
        return orderService.confirmOrder(order);
    }

}