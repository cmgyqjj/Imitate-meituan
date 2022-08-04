package com.hmdp;


import com.hmdp.entity.TradeOrder;
import com.hmdp.service.TradeOrderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = HmDianPingApplication.class)
public class OrderServiceTest {

    @Autowired
    private TradeOrderService orderService;

    @Test
    public void confirmOrder() throws IOException {

        Long coupouId = 345988230098857984L;
        Long goodsId = 345959443973935104L;
        Long userId = 1L;

        TradeOrder order = new TradeOrder();
        order.setGoodsId(goodsId);
        order.setUserId(userId);
        order.setCouponId(coupouId);
        order.setAddress("北京");
        order.setGoodsNumber(1);
        order.setGoodsPrice(new BigDecimal(1000));
        order.setShippingFee(BigDecimal.ZERO);
        order.setOrderAmount(new BigDecimal(1000));
        order.setMoneyPaid(new BigDecimal(100));
//        orderService.confirmOrder(order);

        System.in.read();

    }

}
