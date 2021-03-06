# SixPan

用java写的[6盘](https://v3-beta.6pan.cn/)GUI客户端

## 使用

```bash
git clone https://github.com/zerorooot/SixPan.git
cd SixPan
mvn assembly:assembly
java -jar target/SixPan-1.0-jar-with-dependencies.jar
```



## 目前支持的功能

1. 登录
2. 文件浏览
3. 返回上层目录(点击最上面的lable)
4. 添加离线下载连接
5. 查看离线下载列表
6. 图片浏览
7. 视频浏览(需要安装vlc)
8. 增查删改移文件(右键弹出)
9. 获取文件下载地址(输出至系统剪贴板)

流程

```mermaid
graph LR;
AppMain--启动-->AutoLogin--登录成功-->FileList
AutoLogin--登录失败-->Login
FileList--右键-->基础操作
基础操作---->editItem(重新命名)
基础操作---->createFolderItem(新建文件)
基础操作---->moveFileItem(移动文件)
基础操作---->searchFileItem(搜索文件)
基础操作---->deleteItem(删除文件)
FileList--右键-->高级操作
高级操作---->fileInfoItem(文件信息)
高级操作---->flushItem(刷新文件)
高级操作---->downloadItem(获取链接)
高级操作---->forcePlay(强行播放)
高级操作---->exitLogin(退出登录)
FileList--右键-->离线相关
离线相关---->addOffLineItem(离线下载)
离线相关---->offLineViewltem(离线列表)
offLineViewltem--右键-->offLineAdd(添加离线任务)
offLineViewltem--右键-->deleteComplete(删除已完成任务)
offLineViewltem--右键-->flush(刷新)
offLineViewltem--右键-->deleteCurrent(删除选中任务)
FileList--双击视频文件-->VideoView
VideoView--方向键上-->增加音量
VideoView--方向键下-->减小音量
VideoView--方向键左-->快退15秒
VideoView--方向键右-->快进15秒
VideoView--双击-->全屏模式(在全屏模式下，鼠标上移，可显示视频时间控制条)
VideoView--A键-->顺时针旋转视频
VideoView--D键-->逆时针旋转视频
FileList--双击图片文件-->PictureView
PictureView--方向键左-->上一张
PictureView--方向键右-->下一张
FileList--单击最上面的标签-->返回上一级
FileList--双击文件夹-->进入下一级
```

## 项目目录

```bash
.
├── LICENSE
├── README.md
├── SixPan.iml
├── pom.xml
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── github
    │   │           └── zerorooot
    │   │               ├── AppMain.java
    │   │               ├── bean
    │   │               │   ├── ApiUrl.java
    │   │               │   ├── FileBean.java
    │   │               │   ├── OffLineBean.java
    │   │               │   ├── TableCheckBox.java
    │   │               │   └── TokenBean.java
    │   │               ├── control
    │   │               │   ├── FileControl.java
    │   │               │   └── LoginControl.java
    │   │               ├── serve
    │   │               │   ├── FileServe.java
    │   │               │   └── LoginServe.java
    │   │               ├── util
    │   │               │   ├── ClipBoardUtil.java
    │   │               │   └── PropertiesUtil.java
    │   │               └── view
    │   │                   ├── AutoLogin.java
    │   │                   ├── FileList.java
    │   │                   ├── Login.java
    │   │                   ├── OffLineAddView.java
    │   │                   ├── OffLineTable.java
    │   │                   ├── PictureView.java
    │   │                   └── VideoView.java
    │   └── resources
    │       ├── FileList.fxml
    │       ├── Login.fxml
    │       ├── OffLineAddView.fxml
    │       └── OffLineTable.fxml
    └── test
        └── java
```

