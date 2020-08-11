package com.github.zerorooot.serve;

import com.github.zerorooot.bean.TokenBean;
import com.github.zerorooot.control.LoginControl;

/**
 * @Author: zero
 * @Date: 2020/8/6 10:06
 */
public class LoginServe {
    public TokenBean login(String account, String password) {
        LoginControl loginControl = new LoginControl(account, password);
        return loginControl.login();
    }
}
