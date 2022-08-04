package com.hmdp.service;

import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.TbUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.entity.TradeUserMoneyLog;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author qjj
 * @since 2022-08-04
 */
public interface TbUserService extends IService<TbUser> {
    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sendCode(String phone, HttpSession session);

    Result sign();

    Result signCount();

    Result logout(String token);

    Result updateMoneyPaid(TradeUserMoneyLog userMoneyLog);

}
