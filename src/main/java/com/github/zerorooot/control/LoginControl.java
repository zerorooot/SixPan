package com.github.zerorooot.control;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import com.github.zerorooot.bean.ApiUrl;
import com.github.zerorooot.bean.TokenBean;
import lombok.AllArgsConstructor;

/**
 * @Author: zero
 * @Date: 2020/8/6 10:06
 */
@AllArgsConstructor
public class LoginControl {
    private final String account;
    private final String password;

    public TokenBean login() {
        TokenBean tokenBean = new TokenBean(account, password, null);
        tokenBean.setToken(getToken());
        return tokenBean;
    }

    private String getToken() {
        String url = ApiUrl.LOGIN;
        String md5Password = SecureUtil.md5(password);
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("countryCode", "86");
        jsonObject.set("user", account);
        jsonObject.set("password", md5Password);
        HttpRequest loginPost = HttpUtil.createPost(url);
        loginPost.body(jsonObject.toString());
        HttpResponse loginHttpResponse = loginPost.execute();
        String cookie = loginHttpResponse.getCookies().toString().replace("[", "").replace("]", "");
        JSONObject resultJson = new JSONObject(loginHttpResponse.body());
        if (resultJson.getBool("success")){
            String checkCookieUrl = ApiUrl.CHECK_COOKIE;
            HttpRequest checkCookieGet = HttpUtil.createGet(checkCookieUrl);
            checkCookieGet.header("cookie", cookie);
            checkCookieGet.setFollowRedirects(false);
            HttpResponse execute = checkCookieGet.execute();
            String oauthLoginUrl = execute.header("location");

            HttpRequest oauthLogin = HttpUtil.createGet(oauthLoginUrl);
            HttpResponse oauthLoginResponse = oauthLogin.execute();
            String c = oauthLoginResponse.headerList("set-cookie").toString().replace("[", "").replace("]", "");
            String token = null;
            for (String s : c.split(";")) {
                if (s.contains("token=")) {
                    token = s.split("token=")[1];
                }
            }
            token = "Bearer " + token;
            return token;
        }
        // {"message":"Login Failed","reference":"LOGIN_FAILED","success":false,"status":401}
        return null;

        //{"success":false,"status":401,"reference":"UNAUTHORIZED","message":"无法访问资源，可能需要登录"}
    }

}
