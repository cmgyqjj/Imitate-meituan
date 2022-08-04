package com.hmdp;

import com.hmdp.entity.TbShop;
import com.hmdp.service.TbShopService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.SHOP_GEO_KEY;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private TbShopService shopService;

    @Test
    void loadShopData(){
        List<TbShop> list = shopService.list();
        Map<Long, List<TbShop>> map = list.stream().collect(Collectors.groupingBy(TbShop::getTypeId));
        for(Map.Entry<Long,List<TbShop>> entry:map.entrySet()){
//            获取类型id
            Long typeId=entry.getKey();
            String key=SHOP_GEO_KEY+typeId;
//            获取同类型的店铺的集合
            List<TbShop> value = entry.getValue();
            List<RedisGeoCommands.GeoLocation<String>> locations=new ArrayList<>(value.size());
//            写入redis GEOADD key 经度 纬度 member
            for (TbShop shop :value){
                locations.add(new RedisGeoCommands.GeoLocation<>(
                        shop.getId().toString(),
                        new Point(shop.getX(),shop.getY())
                ));
            }
            stringRedisTemplate.opsForGeo().add(key,locations);
        }
    }

    @Test
    void testHyperLogLog(){
        String[] values = new String[1000];
        int j=0;
        for(int i=0;i<1000000;i++){
            j=i%1000;
            values[j]="user_"+i;
            if(j==999){
                stringRedisTemplate.opsForHyperLogLog().add("hl2",values);
            }
        }
        Long count = stringRedisTemplate.opsForHyperLogLog().size("hl2");
        System.out.println("count="+count);
    }
}
