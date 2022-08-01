package com.hmdp.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author:{QJJ}
 * @date:{2022}
 * @description:
 **/
@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(){
//        配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.56.100:6379");
//        创建RedissonClient对象
        return Redisson.create(config);
    }

}
