package com.github.zerorooot.serve;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.github.zerorooot.bean.FileBean;
import com.github.zerorooot.bean.ImageParameterBean;
import com.github.zerorooot.bean.OffLineBean;
import com.github.zerorooot.control.FileControl;

import java.util.ArrayList;

/**
 * @Author: zero
 * @Date: 2020/8/6 10:05
 */

public class FileServe {
    private final FileControl fileControl;

    public FileServe(String token) {
        fileControl = new FileControl(token);
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
     */
    public void delete(ArrayList<FileBean> fileBeanArrayList) {
         fileControl.delete(fileBeanArrayList);
    }

    /**
     * 删除文件
     *
     * @param fileBean 要删除的fileBean
     */
    public void delete(FileBean fileBean) {
        ArrayList<FileBean> fileBeans = new ArrayList<>();
        fileBeans.add(fileBean);
        fileControl.delete(fileBeans);
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
     * @return error message
     */
    public String move(ArrayList<FileBean> fileBeanArrayList, String newPath) {
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
     * @return download url
     */
    public String download(FileBean fileBean) {
        return fileControl.download(fileBean.getIdentity());
    }

    /**
     *  打包下载
     * @param fileBeanArrayList file list
     * @return download url
     */
    public String download(ArrayList<FileBean> fileBeanArrayList )  {
        return fileControl.download(fileBeanArrayList);
    }

    /**
     * 图片预览
     * @param fileBean file bean
     * @return image
     */
    public ImageParameterBean imagePreview(FileBean fileBean) {
        return fileControl.imagePreview(fileBean.getIdentity());
    }

    /**
     * 获取下载地址
     *
     * @param offLineBean offLineBean
     * @return download url
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
     */
    public void addOffLine(String path, String text, String password) {
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

         fileControl.addOffLine(jsonObject.toString());
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
    public void deleteComplete() {
         fileControl.deleteComplete();
    }

    /**
     * 删除某离线文件
     *
     * @param offLineBeanArrayList offLineBeanArrayList
     */
    public void offLineDelete(ArrayList<OffLineBean> offLineBeanArrayList) {
        fileControl.offLineDelete(offLineBeanArrayList);
    }

    /**
     * 删除某离线文件
     * @param offLineBean  offLineBean
     */
    public void offLineDelete(OffLineBean offLineBean) {
        ArrayList<OffLineBean> offLineBeans = new ArrayList<>();
        offLineBeans.add(offLineBean);
        fileControl.offLineDelete(offLineBeans);
    }
    /**
     * 搜索文件
     *
     * @param name 文件名
     * @return file list
     */
    public ArrayList<FileBean> searchFile(String name) {
        return fileControl.searchFile(name);
    }

    /**
     * 搜索文件
     * @param parentIdentity parentIdentity
     * @param name 文件名
     * @return ArrayList
     */
    public ArrayList<FileBean> searchFile(String parentIdentity, String name) {
        return fileControl.searchFile(parentIdentity, name);
    }

}
