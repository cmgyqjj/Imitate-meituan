package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.service.TbShopService;
import com.hmdp.service.TbShopTypeService;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/shop-type")
public class TbShopTypeController {
    @Resource
    private TbShopTypeService typeService;

    @GetMapping("list")
    public Result queryTypeList() {
        Result typeResult = typeService.queryList();
        return typeResult;
    }
}

