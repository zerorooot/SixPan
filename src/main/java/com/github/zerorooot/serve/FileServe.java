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

    public ArrayList<FileBean> getFileAll(String parentPath)  {
        return fileControl.getFileAll(parentPath);
    }
    public ArrayList<FileBean> getDirectory(String parentPath) {
        return fileControl.getDirectory(parentPath);
    }

    public ArrayList<FileBean> getNonDirectory(String parentPath) {
        return fileControl.getNonDirectory(parentPath);
    }

    public int delete(ArrayList<FileBean> fileBeanArrayList) {
        return fileControl.delete(fileBeanArrayList);
    }

    public void rename(FileBean fileBean, String newName) {
        fileControl.rename(fileBean, newName);
    }

    public int move(ArrayList<FileBean> fileBeanArrayList, String newPath) {
        return fileControl.move(fileBeanArrayList, newPath);
    }

    public void createFolder(String path, String folderName) {
        fileControl.createFolder(path, folderName);
    }

    public String download(FileBean fileBean) {
        return fileControl.download(fileBean.getIdentity());
    }

    public String download(OffLineBean offLineBean) {
        return fileControl.download(offLineBean.getAccessIdentity());
    }
    public String download(String identity) {
        return fileControl.download(identity);
    }

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

    public ArrayList<OffLineBean> getOffLine() {
        return fileControl.getOffLine();
    }

    public String quota() {
        return fileControl.quota();
    }

    public int deleteComplete() {
        return fileControl.deleteComplete();
    }
    public int offLineDelete(ArrayList<OffLineBean> offLineBeanArrayList) {
        return fileControl.offLineDelete(offLineBeanArrayList);
    }

}
