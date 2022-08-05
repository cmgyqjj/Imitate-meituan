package com.hmdp.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import com.github.xiaoymin.swaggerbootstrapui.annotations.EnableSwaggerBootstrapUI;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;

import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
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
@EnableOpenApi
@EnableSwaggerBootstrapUI
@EnableKnife4j
public class SwaggerConfig {

//    @Value("${sny.security.jwt.header}")
//    private String tokenHeader;


    private Boolean enabled=true;

    @Bean
    public Docket createRestApi() {
        Docket docket = new Docket(DocumentationType.OAS_30);
        docket.enable(enabled).apiInfo(apiInfo())
                // 分组名称
                .groupName("all").select()
                // 扫描包路径
                .apis(RequestHandlerSelectors.basePackage("com.hmdp.controller"))
                .paths(PathSelectors.any()).build();
//                .globalRequestParameters(globalRequestParameters());
//		docket.additionalModels(null, null)
        return docket;
    }

//    private List<RequestParameter> globalRequestParameters() {
//        RequestParameterBuilder ticketPar = new RequestParameterBuilder();
//        ticketPar.name(tokenHeader).description("token").in(ParameterType.HEADER).required(true).build();
//        List<RequestParameter> pars = new ArrayList<RequestParameter>();
//        pars.add(ticketPar.build());
//        return pars;
//    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("接口文档")
                .description("所有业务接口文档")
                .version("4.0").build();
    }

}
