package com.github.zerorooot.control;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.github.zerorooot.bean.ApiUrl;
import com.github.zerorooot.bean.FileBean;
import com.github.zerorooot.bean.OffLineBean;
import com.github.zerorooot.bean.TableCheckBox;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @Author: zero
 * @Date: 2020/8/6 10:06
 */
@AllArgsConstructor
public class FileControl {
    private final String token;

    //{"directory":true,"parentPath":"/新建文件夹","limit":-1}

    /**
     * 获取所有文件
     *
     * @param parentPath 文件路径
     * @return
     */
    public ArrayList<FileBean> getFileAll(String parentPath) {
        return getFile(parentPath, null);
    }

    /**
     * 获取文件目录
     *
     * @param parentPath 文件路径
     * @return
     */
    public ArrayList<FileBean> getDirectory(String parentPath) {
        return getFile(parentPath, true);
    }

    /**
     * 获取文件名
     *
     * @param parentPath 文件路径
     * @return file name
     */
    public ArrayList<FileBean> getNonDirectory(String parentPath) {
        return getFile(parentPath, false);
    }

    /**
     * 获取文件
     *
     * @param parentPath 文件路径
     * @param directory  是否是目录
     * @return
     */
    public ArrayList<FileBean> getFile(String parentPath, Boolean directory) {
        String url = ApiUrl.LIST;
        JSONObject bodyJson = new JSONObject();
        bodyJson.set("parentPath", parentPath);
        bodyJson.set("limit", -1);
        if (Objects.nonNull(directory)) {
            bodyJson.set("directory", directory);
        }

        HttpRequest post = HttpUtil.createPost(url);
        post.header("authorization", token);

        post.body(bodyJson.toString());
        JSONObject object = new JSONObject(post.execute().body());
        JSONArray dataList = object.getJSONArray("dataList");
        ArrayList<FileBean> fileBeanLinkedList = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            FileBean fileBean = setTimeAndPath(dataList, i);

            long size = fileBean.getSize();
            fileBean.setSizeString(DataSizeUtil.format(size));

            fileBean.setCheckBox(new TableCheckBox());

            fileBeanLinkedList.add(fileBean);
        }
        return fileBeanLinkedList;
    }

    /**
     * 删除文件
     *
     * @param fileBeanArrayList 要删除的list
     */
    public void delete(ArrayList<FileBean> fileBeanArrayList) {
        String url = ApiUrl.DELETE;
        HttpRequest post = HttpUtil.createPost(url);
        post.header("authorization", token);
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        fileBeanArrayList.forEach(s -> jsonArray.add(s.getIdentity()));
        jsonObject.set("sourceIdentity", jsonArray);
        post.body(jsonObject.toString());
        post.executeAsync();

    }

    /**
     * 重命名文件
     *
     * @param fileBean 要重命名的bean
     * @param newName  新文件名
     */
    public void rename(FileBean fileBean, String newName) {
        String url = ApiUrl.RENAME;
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("identity", fileBean.getIdentity());
        jsonObject.set("name", newName);
        HttpRequest post = HttpUtil.createPost(url);
        post.header("authorization", token);
        post.body(jsonObject.toString());
        post.executeAsync();
    }

    /**
     * 移动文件
     *
     * @param fileBeanArrayList 要移动的list
     * @param newPath           移动的目录
     */
    public void move(ArrayList<FileBean> fileBeanArrayList, String newPath) {
        String url = ApiUrl.MOVE;
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        fileBeanArrayList.forEach(s -> jsonArray.add(s.getIdentity()));
        jsonObject.set("sourceIdentity", jsonArray);
        jsonObject.set("path", newPath);
        HttpRequest post = HttpUtil.createPost(url);
        post.header("authorization", token);
        post.body(jsonObject.toString());
        post.executeAsync();
    }

    /**
     * 创建新文件夹
     *
     * @param path       新文件夹的目录
     * @param folderName 新文件夹名
     */
    public void createFolder(String path, String folderName) {
        String url = ApiUrl.CREATEFOLDER;
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("path", path);
        jsonObject.set("name", folderName);
        HttpRequest post = HttpUtil.createPost(url);
        post.header("authorization", token);
        post.body(jsonObject.toString()).executeAsync();
    }

    /**
     * 获取文件下载url
     *
     * @param identity 文件识别号
     * @return
     */
    @SneakyThrows
    public String download(String identity) {
        String url = ApiUrl.DOWNLOAD;
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("identity", identity);
        HttpRequest post = HttpUtil.createPost(url);
        post.header("authorization", token);
        post.body(jsonObject.toString());

        JSONObject returnJson = new JSONObject(post.execute().body());
        while (returnJson.getStr("success") != null) {
            Thread.sleep(100);
            returnJson = new JSONObject(post.execute().body());
        }
        return returnJson.getStr("downloadAddress");
    }

    /**
     * 查看当前离线下载的配额
     *
     * @return
     */
    public String quota() {
        String url = ApiUrl.QUOTA;
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("time", System.currentTimeMillis());
        HttpRequest post = HttpUtil.createPost(url);
        post.header("authorization", token);
        post.body(jsonObject.toString());
        JSONObject returnJson = new JSONObject(post.execute().body());
        return returnJson.getStr("available") + "/" + returnJson.getStr("dailyQuota");
    }

    /**
     * 离线下载百度网盘文件
     *
     * @param textLink 离线下载的url
     * @param password 离线下载的密码
     * @return
     */
    public String parse(String textLink, String password) {
        String url = ApiUrl.PARSE;
        JSONObject jsonObject = new JSONObject();
        HttpRequest post = HttpUtil.createPost(url);
        post.header("authorization", token);
        jsonObject.set("textLink", textLink);
        if (Objects.nonNull(password)) {
            jsonObject.set("password", password);
        }
        post.body(jsonObject.toString());
        JSONObject returnJson = new JSONObject(post.execute().body());
        if (Objects.nonNull(returnJson.getStr("success")) && !returnJson.getBool("success")) {
            throw new RuntimeException(returnJson.toString());
        }
        return returnJson.getStr("hash");
    }

    /**
     * 离线下载文件
     *
     * @param json 文件的url
     * @return
     */
    public void addOffLine(String json) {
        String url = ApiUrl.ADDOFFLINE;
        HttpRequest post = HttpUtil.createPost(url);
        post.header("authorization", token);
        post.body(json);
        post.executeAsync();
    }

    /**
     * 获取离线下载列表
     *
     * @return
     */
    public ArrayList<OffLineBean> getOffLine() {
        String url = ApiUrl.OFFLINELIST;
        HttpRequest post = HttpUtil.createPost(url);
        post.header("authorization", token);
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("limit", -1);
        jsonObject.set("start", 0);
        post.body(jsonObject.toString());
        JSONArray returnJson = new JSONObject(post.execute().body()).getJSONArray("dataList");
        ArrayList<OffLineBean> arrayList = new ArrayList<>();
        for (JSONObject o : returnJson.jsonIter()) {
            OffLineBean offLineBean = o.toBean(OffLineBean.class);
            offLineBean.setTime(DateUtil.date(offLineBean.getCreateTime()).toString());
            if ("text/directory".equals(offLineBean.getFileMime())) {
                offLineBean.setDirectory(true);
            }
            offLineBean.setCheckBox(new TableCheckBox());
            arrayList.add(offLineBean);
        }

        return arrayList;
    }

    /**
     * 把离线已完成的文件从离线列表中移除(不删除文件)
     *
     * @return
     */
    public void deleteComplete() {
        String url = ApiUrl.DELETECOMPLETE;
        HttpRequest post = HttpUtil.createPost(url);
        post.header("authorization", token);
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("type", 1000);
        jsonObject.set("deleteFile", false);
        post.body(jsonObject.toString());
        post.executeAsync();
    }

    /**
     * 删除某离线文件
     *
     * @param offLineBeanArrayList 要删除的list
     * @return
     */
    public void offLineDelete(ArrayList<OffLineBean> offLineBeanArrayList) {
        String url = ApiUrl.OFFLINEDELETE;
        HttpRequest post = HttpUtil.createPost(url);
        post.header("authorization", token);
        JSONArray jsonArray = new JSONArray();
        offLineBeanArrayList.forEach(s -> jsonArray.add(s.getTaskIdentity()));
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("taskIdentity", jsonArray);
        jsonObject.set("deleteFile", true);
        post.body(jsonObject.toString());
        post.executeAsync();
    }

    /**
     * 搜索文件
     *
     * @param fileName 文件名
     * @return
     */
    public ArrayList<FileBean> searchFile(String fileName) {
        String url = ApiUrl.LIST;
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("limit", -1);
        jsonObject.set("name", fileName);
        jsonObject.set("parentIdentity", "");
        jsonObject.set("search", true);
        HttpRequest post = HttpUtil.createPost(url);
        post.header("authorization", token);
        post.body(jsonObject.toString());
        JSONArray returnJson = new JSONObject(post.execute().body()).getJSONArray("dataList");
        ArrayList<FileBean> fileBeanArrayList = new ArrayList<>();

        for (int i = 0; i < returnJson.size(); i++) {
            FileBean fileBean=setTimeAndPath(returnJson, i);
            fileBean.setCheckBox(new TableCheckBox());

            fileBeanArrayList.add(fileBean);
        }
        return fileBeanArrayList;
    }

    /**
     * 设置bean中的文件大小和路径
     * @param returnJson json字符串
     * @param i 位置
     * @return bean
     */
    private FileBean setTimeAndPath(JSONArray returnJson, int i) {
        JSONObject object = returnJson.getJSONObject(i);
        FileBean fileBean = object.toBean(FileBean.class);
        fileBean.setDateTime(DateUtil.date(fileBean.getAtime()).toString());
        String path = fileBean.getPath();
        if (path.lastIndexOf("/") == 0) {
            fileBean.setParentPath("/");
        } else {
            fileBean.setParentPath(path.substring(0, path.lastIndexOf("/")));
        }
        return fileBean;
    }

}
