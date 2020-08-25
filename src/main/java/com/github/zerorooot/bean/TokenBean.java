package com.github.zerorooot.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Author: zero
 * @Date: 2020/8/7 18:56
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TokenBean {
    private String account;
    private String password;
    private String token;
}
