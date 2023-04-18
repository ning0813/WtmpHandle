package com.example.wtmphandle.bean;

import lombok.Data;

/**
 * @ClassName： Result
 * @description: 登录用户信息
 * @author: ning.yang
 * @create: 2023/4/13 11:37
 */
@Data
public class LoginInfo {

    // 登录用户 登录方式  ip 登入时间 登出时间  登录时长

    private String huaan_host;

    private String username;

    private String loginType;

    private String src_ip;

    private String login_time;

    private String logout_time;

    private String cost_time;

    private String message;


}
