package com.github.zerorooot.bean;

import javafx.scene.control.CheckBox;
import lombok.*;


import java.io.Serializable;

/**
 * @Author: zero
 * @Date: 2020/8/6 9:43
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
@NoArgsConstructor
public class FileBean implements Serializable {
    private String path;
    private String name;
    private boolean directory;
    private long size;
    private String identity;
    private long atime;
    private String dateTime;
    private String mime;

    private String parentPath;
    private TableCheckBox checkBox;
    private String sizeString;
}
