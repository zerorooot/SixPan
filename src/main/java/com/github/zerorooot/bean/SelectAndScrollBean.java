package com.github.zerorooot.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ubuntu
 */
@AllArgsConstructor
@Getter
public class SelectAndScrollBean {
    private final int selectRow;
    private final int scroll;
}
