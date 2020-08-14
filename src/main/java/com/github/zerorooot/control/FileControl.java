package com.github.zerorooot.control;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.github.zerorooot.bean.ApiUrl;
import com.github.zerorooot.bean.FileBean;
import com.github.zerorooot.bean.OffLineBean;
import com.github.zerorooot.bean.TableCheckBox;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @Author: zero
 * @Date: 2020/8/6 10:06
 */
@AllArgsConstructor
public class FileControl {
    private final String cookie;

    //{"directory":true,"parentPath":"/新建文件夹","limit":-1}
    public ArrayList<FileBean> getFileAll(String parentPath) {
        return getFile(parentPath, null);
    }

    public ArrayList<FileBean> getDirectory(String parentPath) {
        return getFile(parentPath, true);
    }

    public ArrayList<FileBean> getNonDirectory(String parentPath) {
        return getFile(parentPath, false);
    }

    public ArrayList<FileBean> getFile(String parentPath, Boolean directory) {
        String url = ApiUrl.LIST;
        JSONObject bodyJson = new JSONObject();
        bodyJson.set("parentPath", parentPath);
        bodyJson.set("limit", -1);
        if (Objects.nonNull(directory)) {
            bodyJson.set("directory", directory);
        }

        HttpRequest post = HttpUtil.createPost(url);
        post.header("cookie", cookie);

        post.body(bodyJson.toString());
        JSONObject object = new JSONObject(post.execute().body());
        JSONArray dataList = object.getJSONArray("dataList");
        ArrayList<FileBean> fileBeanLinkedList = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            JSONObject jsonObject = dataList.getJSONObject(i);
            FileBean fileBean = jsonObject.toBean(FileBean.class);
            fileBean.setDateTime(DateUtil.date(fileBean.getAtime()).toString());
            String path = fileBean.getPath();
            if (path.lastIndexOf("/") == 0) {
                fileBean.setParentPath("/");
            }else {
                fileBean.setParentPath(path.substring(0, path.lastIndexOf("/")));
            }
            fileBean.setCheckBox(new TableCheckBox());

            fileBeanLinkedList.add(fileBean);
        }
        return fileBeanLinkedList;
    }


    public int delete(ArrayList<FileBean> fileBeanArrayList) {
        String url = ApiUrl.DELETE;
        HttpRequest post = HttpUtil.createPost(url);
        post.header("cookie", cookie);
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        fileBeanArrayList.forEach(s -> jsonArray.add(s.getIdentity()));
        jsonObject.set("sourceIdentity", jsonArray);
        JSONObject returnJson = new JSONObject(post.body(jsonObject.toString()).execute().body());
        return returnJson.getInt("successCount");

    }

    public void rename(FileBean fileBean, String newName) {
        String url = ApiUrl.RENAME;
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("identity", fileBean.getIdentity());
        jsonObject.set("name", newName);
        HttpRequest post = HttpUtil.createPost(url);
        post.header("cookie", cookie);
        post.body(jsonObject.toString());
        post.executeAsync();
    }

    public int move(ArrayList<FileBean> fileBeanArrayList, String newPath) {
        String url = ApiUrl.MOVE;
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        fileBeanArrayList.forEach(s -> jsonArray.add(s.getIdentity()));
        jsonObject.set("sourceIdentity", jsonArray);
        jsonObject.set("path", newPath);
        HttpRequest post = HttpUtil.createPost(url);
        post.header("cookie", cookie);
        JSONObject returnJson = new JSONObject(post.body(jsonObject.toString()).execute().body());
        return returnJson.getInt("successCount");
    }

    public void createFolder(String path, String folderName) {
        String url = ApiUrl.CREATEFOLDER;
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("path", path);
        jsonObject.set("name", folderName);
        HttpRequest post = HttpUtil.createPost(url);
        post.header("cookie", cookie);
        post.body(jsonObject.toString()).executeAsync();
    }

    public String download(String  identity) {
        String url = ApiUrl.DOWNLOAD;
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("identity", identity);
        HttpRequest post = HttpUtil.createPost(url);
        post.header("cookie", cookie);
        post.body(jsonObject.toString());

        JSONObject returnJson = new JSONObject(post.execute().body());
        return returnJson.getStr("downloadAddress");
    }

    public String quota() {
        String url = ApiUrl.QUOTA;
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("time", System.currentTimeMillis());
        HttpRequest post = HttpUtil.createPost(url);
        post.header("cookie", cookie);
        post.body(jsonObject.toString());
        JSONObject returnJson = new JSONObject(post.execute().body());
        return returnJson.getStr("available") + "/" + returnJson.getStr("dailyQuota");
    }


    public String parse(String textLink, String password) {
        String url = ApiUrl.PARSE;
        JSONObject jsonObject = new JSONObject();
        HttpRequest post = HttpUtil.createPost(url);
        post.header("cookie", cookie);
        jsonObject.set("textLink", textLink);
        if (Objects.nonNull(password)) {
            jsonObject.set("password", password);
        }
        post.body(jsonObject.toString());
        JSONObject returnJson = new JSONObject(post.execute().body());
        if (Objects.nonNull(returnJson.getStr("success"))&&!returnJson.getBool("success")) {
            throw new RuntimeException(returnJson.toString());
        }
        return returnJson.getStr("hash");
    }

    public int addOffLine(String json) {
        String url = ApiUrl.ADDOFFLINE;
        HttpRequest post = HttpUtil.createPost(url);
        post.header("cookie", cookie);
        JSONObject returnJson = new JSONObject(post.body(json).execute().body());
        return returnJson.getInt("successCount");
    }

    public ArrayList<OffLineBean> getOffLine() {
        String url = ApiUrl.OFFLINELIST;
        HttpRequest post = HttpUtil.createPost(url);
        post.header("cookie", cookie);
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

    public int deleteComplete() {
        String url = ApiUrl.DELETECOMPLETE;
        HttpRequest post = HttpUtil.createPost(url);
        post.header("cookie", cookie);
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("type", 1000);
        jsonObject.set("deleteFile", false);
        post.body(jsonObject.toString());
        JSONObject returnJson = new JSONObject(post.execute().body());
        return returnJson.getInt("successCount");
    }

    public int offLineDelete(ArrayList<OffLineBean> offLineBeanArrayList) {
        String url = ApiUrl.OFFLINEDELETE;
        HttpRequest post = HttpUtil.createPost(url);
        post.header("cookie", cookie);
        JSONArray jsonArray = new JSONArray();
        offLineBeanArrayList.forEach(s -> jsonArray.add(s.getTaskIdentity()));
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("taskIdentity", jsonArray);
        jsonObject.set("deleteFile", true);
        post.body(jsonObject.toString());
        JSONObject returnJson = new JSONObject(post.execute().body());
        return returnJson.getInt("successCount");
    }

}
