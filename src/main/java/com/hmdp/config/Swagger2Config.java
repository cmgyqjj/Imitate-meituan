package com.hmdp.config;

import com.github.xiaoymin.swaggerbootstrapui.annotations.EnableSwaggerBootstrapUI;
import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;

import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author
 */

@Configuration
//开启swagger2
@EnableSwagger2
public class Swagger2Config {
    @Bean
    public Docket createRestApi() {

        return new Docket(DocumentationType.SWAGGER_2)          //指定Api类型为Swagger2
                .apiInfo(apiInfo())   //指定文档汇总信息
                .select()
                .apis(RequestHandlerSelectors
                        .basePackage("com.hmdp.controller")) //指定controller包路径
                .paths(PathSelectors.any())  //指定展示所有controller
                .build();
    }

    private ApiInfo apiInfo() {
        //返回一个apiinfo
        return new ApiInfoBuilder()
                .title("api接口文档")                                       //文档页标题
                .contact(
                        new Contact("qjj", "http://softeem.com", "524733312@qq.com")
                )                                                           // 联系人信息
                .description("api文档描述")          // 描述信息
                .version("1.0.1")              // 文档版本号
                .termsOfServiceUrl("https://www.baidu.com") //服务地址
                .build();
    }
}
