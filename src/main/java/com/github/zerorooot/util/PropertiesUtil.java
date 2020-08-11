package com.github.zerorooot.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @Author: zero
 * @Date: 2020/8/6 10:26
 */
public class PropertiesUtil {

    public static Properties getProperties() throws IOException {
        String location =
                new File(PropertiesUtil.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile().toString();
        String path = location + File.separator + "init.properties";
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        Properties properties = new Properties();
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8);
        properties.load(inputStreamReader);
        return properties;
    }

    public static void setProperties(Properties properties) throws IOException {
        String location =
                new File(PropertiesUtil.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile().toString();
        String path = location + File.separator + "init.properties";
        properties.store(new FileOutputStream(path), null);
    }

}
