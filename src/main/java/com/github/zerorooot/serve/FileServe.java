package com.github.zerorooot.serve;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.github.zerorooot.bean.FileBean;
import com.github.zerorooot.bean.OffLineBean;
import com.github.zerorooot.control.FileControl;

import java.util.ArrayList;

/**
 * @Author: zero
 * @Date: 2020/8/6 10:05
 */

public class FileServe {
    private final FileControl fileControl;

    public FileServe(String cookie) {
        fileControl = new FileControl(cookie);
    }

    /**
     * 获取所有文件
     *
     * @param parentPath 路径
     * @return
     */
    public ArrayList<FileBean> getFileAll(String parentPath) {
        return fileControl.getFileAll(parentPath);
    }

    /**
     * 获取文件夹
     *
     * @param parentPath 路径
     * @return
     */
    public ArrayList<FileBean> getDirectory(String parentPath) {
        return fileControl.getDirectory(parentPath);
    }

    /**
     * 获取非文件夹
     *
     * @param parentPath 路径
     * @return
     */
    public ArrayList<FileBean> getNonDirectory(String parentPath) {
        return fileControl.getNonDirectory(parentPath);
    }

    /**
     * 删除文件
     *
     * @param fileBeanArrayList 要删除的list
     * @return
     */
    public int delete(ArrayList<FileBean> fileBeanArrayList) {
        return fileControl.delete(fileBeanArrayList);
    }

    /**
     * 重命名文件
     *
     * @param fileBean 要重命名的bean
     * @param newName  新名字
     */
    public void rename(FileBean fileBean, String newName) {
        fileControl.rename(fileBean, newName);
    }

    /**
     * 移动文件
     *
     * @param fileBeanArrayList 要移动的list
     * @param newPath           新目录
     * @return
     */
    public int move(ArrayList<FileBean> fileBeanArrayList, String newPath) {
        return fileControl.move(fileBeanArrayList, newPath);
    }

    /**
     * 创建文件夹
     *
     * @param path       要创建的路径
     * @param folderName 文件夹名称
     */
    public void createFolder(String path, String folderName) {
        fileControl.createFolder(path, folderName);
    }

    /**
     * 获取下载地址
     *
     * @param fileBean bean
     * @return
     */
    public String download(FileBean fileBean) {
        return fileControl.download(fileBean.getIdentity());
    }

    /**
     * 获取一堆文件的下载地址
     *
     * @param fileBeanArrayList list
     * @return 下载地址
     */
    public String download(ArrayList<FileBean> fileBeanArrayList) {
        StringBuffer stringBuffer = new StringBuffer();
        fileBeanArrayList.forEach(s -> {
            if (!s.isDirectory()) {
                stringBuffer.append(fileControl.download(s.getIdentity()));
                stringBuffer.append("\n");
            }
        });
        return "\n".equals(stringBuffer.toString()) ? null : stringBuffer.toString();

    }

    /**
     * 获取下载地址
     *
     * @param offLineBean offlinebean
     * @return
     */
    public String download(OffLineBean offLineBean) {
        return fileControl.download(offLineBean.getAccessIdentity());
    }

    /**
     * 添加离线下载
     *
     * @param path     要离线下载的路径
     * @param text     下载的url
     * @param password 下载的密码
     * @return
     */
    public int addOffLine(String path, String text, String password) {
        String[] split = text.split("\n");
        JSONArray jsonArray = new JSONArray();
        for (String s : split) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("hash", fileControl.parse(s, password));
            jsonArray.add(jsonObject);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("task", jsonArray);
        jsonObject.set("savePath", path);

        return fileControl.addOffLine(jsonObject.toString());
    }

    /**
     * 获取离线下载列表
     *
     * @return
     */
    public ArrayList<OffLineBean> getOffLine() {
        return fileControl.getOffLine();
    }

    /**
     * 获取离线下载的配额
     *
     * @return
     */
    public String quota() {
        return fileControl.quota();
    }

    /**
     * 把离线已完成的文件从离线列表中移除(不删除文件)
     *
     * @return
     */
    public int deleteComplete() {
        return fileControl.deleteComplete();
    }

    /**
     * 删除某离线文件
     *
     * @param offLineBeanArrayList
     * @return
     */
    public int offLineDelete(ArrayList<OffLineBean> offLineBeanArrayList) {
        return fileControl.offLineDelete(offLineBeanArrayList);
    }

    /**
     * 搜索文件
     *
     * @param name 文件名
     * @return
     */
    public ArrayList<FileBean> searchFile(String name) {
        return fileControl.searchFile(name);
    }

}
