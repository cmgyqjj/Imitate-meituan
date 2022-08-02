package com.hmdp.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserDTO {
    private Long id;
    private String nickName;
    private String icon;
    private String city;
    private String introduce;
    private Integer userScore;
    private Long userMoney;
    private Integer fans;
    private Integer followee;
    private Integer level;
    private Date birthday;
    private Boolean gender;
}
