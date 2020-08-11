package com.github.zerorooot.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Author: zero
 * @Date: 2020/8/9 20:43
 */
@AllArgsConstructor
@Setter
@Getter
public class OffLineBean {
    private String name;
    private String savePath;
    private String accessPath;
    private String textLink;
    private int progress;
    private long size;
    private long createTime;
    private String time;
    private String fileMime;
    private boolean directory=true;
    private TableCheckBox checkBox;
    private String taskIdentity;
}
