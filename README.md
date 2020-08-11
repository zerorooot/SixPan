# SixPan

用java写的[6盘](https://v3-beta.6pan.cn/)GUI客户端

## 使用

```bash
git clone git@github.com:zerorooot/SixPan.git
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
8. 增删改移文件(右键弹出)

流程

```mermaid
graph LR;
AppMain--启动-->LoginView--登录-->FileView
FileView--右键-->editItem(重新命名)
FileView--右键-->createFolderItem(新建文件)
FileView--右键-->moveFileItem(移动文件)
FileView--右键-->flushItem(刷新文件)
FileView--右键-->deleteItem(删除文件)
FileView--右键-->addOffLineItem(离线下载)
FileView--右键-->offLineViewltem(离线列表)
offLineViewltem--右键-->offLineAdd(添加离线任务)
offLineViewltem--右键-->deleteComplete(删除已完成任务)
offLineViewltem--右键-->flush(刷新)
offLineViewltem--右键-->deleteCurrent(删除选中任务)
FileView--双击视频文件-->VideoView
VideoView--方向键上-->增加音量
VideoView--方向键下-->减小音量
VideoView--方向键左-->快退15秒
VideoView--方向键右-->快进15秒
FileView--双击图片文件-->PictureView
PictureView--方向键左-->上一张
PictureView--方向键右-->下一张
FileView--单击最上面的标签-->返回上一级
FileView--双击文件夹-->进入下一级
```

## 项目目录

```bash
.
├── LICENSE
├── README.md
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
    │   │               │   └── PropertiesUtil.java
    │   │               └── view
    │   │                   ├── FileView.java
    │   │                   ├── LoginView.java
    │   │                   ├── OffLineAddView.java
    │   │                   ├── OffLineTableView.java
    │   │                   ├── PictureView.java
    │   │                   └── VideoView.java
    │   └── resources
    │       └── OffLineAddView.fxml
    └── test
        └── java
```

