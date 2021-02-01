package com.github.zerorooot.view;

import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.http.HttpUtil;
import com.github.zerorooot.bean.FileBean;
import com.github.zerorooot.bean.OffLineBean;
import com.github.zerorooot.serve.FileServe;
import com.github.zerorooot.util.ClipBoardUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author: zero
 * @Date: 2020/8/19 21:13
 * TODO
 * 进入下一级loading
 */
@NoArgsConstructor
public class FileList implements Initializable {
    public TableColumn<FileBean, String> nameTableColumn;
    public TableColumn<FileBean, String> sizeTableColumn;
    public TableColumn<FileBean, String> createTimeColumn;
    public TableView<FileBean> table;
    public Label label;
    public ContextMenu contextMenu;
    private String token;
    private FileServe fileServe;
    private final ObservableList<FileBean> fileBeanObservableList = FXCollections.observableArrayList();
    private boolean existOffLineTable;
    private int currentSelectionRow;
    private Stage offLineTableStage;

    public void setToken(String token) {
        this.token = token;
    }

    public FileList(String token) {
        this.token = token;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fileServe = new FileServe(token);

        //设置column大小
        table.setColumnResizePolicy(resizeFeatures -> {
            nameTableColumn.setPrefWidth(table.widthProperty().get() * 0.7);
            sizeTableColumn.setPrefWidth(table.widthProperty().get() * 0.126);
            createTimeColumn.setPrefWidth(table.widthProperty().get() * 0.155);
            return true;
        });
        //设置column数值
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameTableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameTableColumn.setEditable(true);
        //创建时间
        createTimeColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        //文件大小
        sizeTableColumn.setCellValueFactory(new PropertyValueFactory<>("sizeString"));
        //比较文件大小，方便排序
        sizeTableColumn.setComparator((o1, o2) -> {
            try {
                long o1Long = DataSizeUtil.parse(o1.replace(" ", "").replace(",", ""));
                long o2Long = DataSizeUtil.parse(o2.replace(" ", "").replace(",", ""));
                return o1Long > o2Long ? 0 : 1;
            } catch (Exception e) {
                System.err.println(o1 + "           " + o2);
            }
            return 0;
        });


        //输出数值
        ArrayList<FileBean> fileBeanArrayList = fileServe.getFileAll("/");
        fileBeanObservableList.addAll(fileBeanArrayList);
        table.setItems(fileBeanObservableList);

        //设置右键菜单
        table.setOnContextMenuRequested(event -> {
            contextMenu.show(table, event.getScreenX(), event.getScreenY());
        });

        // shift选择
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    /**
     * Column重命名
     *
     * @param t CellEditEvent
     */
    public void rename(TableColumn.CellEditEvent<FileBean, String> t) {
        Platform.runLater(() -> {
            FileBean fileBean = t.getRowValue();
            (t.getTableView().getItems().get(t.getTablePosition().getRow())).setName(t.getNewValue());
            fileServe.rename(fileBean, t.getNewValue());
            flush();
        });
    }

    /**
     * 重命名某文件
     *
     * @param actionEvent ActionEvent
     */
    public void getEditItem(ActionEvent actionEvent) {
        editFileName();
    }

    /**
     * 创建新文件
     *
     * @param actionEvent ActionEvent
     */
    public void getCreateFolderItem(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("新建文件夹");
        dialog.setHeaderText("文件夹名");
        dialog.setContentText("请输入新的文件夹的名称：");
        Optional<String> result = dialog.showAndWait();

        //刷新
        result.ifPresent(name -> {
            fileServe.createFolder(label.getText(), name);
            fileBeanObservableList.clear();
            fileBeanObservableList.addAll(fileServe.getFileAll(label.getText()));
            table.setItems(fileBeanObservableList);
        });
    }

    /**
     * 移动文件
     *
     * @param actionEvent actionEvent
     */
    public void getMoveFileItem(ActionEvent actionEvent) {
        ArrayList<FileBean> moveFileBeanArrayList = getSelectFileBeanArrayList();

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("移动文件");

        ButtonType removeButtonType = new ButtonType("移动", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(removeButtonType, ButtonType.CANCEL);

        
        Label moveLabel = new Label("/");
        moveLabel.setMaxWidth(Double.MAX_VALUE);

        TableView<FileBean> moveTable = new TableView<>();
        TableColumn<FileBean, String> nameTableColumn = new TableColumn<>("名称");
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameTableColumn.setMaxWidth(Double.MAX_VALUE);
        moveTable.getColumns().add(nameTableColumn);
        moveTable.setColumnResizePolicy(resizeFeatures -> {
            nameTableColumn.setPrefWidth(moveTable.widthProperty().get());
            return true;
        });

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(moveLabel);
        borderPane.setCenter(moveTable);

        ObservableList<FileBean> moveFileBeanObservableList = FXCollections.observableArrayList();
        ArrayList<FileBean> directoryFileBeanArrayList = fileServe.getDirectory(moveLabel.getText());
        moveFileBeanObservableList.addAll(directoryFileBeanArrayList);
        moveTable.setItems(moveFileBeanObservableList);

        //双击进入下一级
        moveTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                FileBean fileBean = moveTable.getSelectionModel().getSelectedItem();
                moveFileBeanObservableList.clear();
                moveFileBeanObservableList.addAll(fileServe.getDirectory(fileBean.getPath()));
                moveTable.setItems(moveFileBeanObservableList);
                moveLabel.setText(fileBean.getPath());
            }
        });
        //返回上一级
        moveLabel.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                String path = moveLabel.getText();
                if (path.lastIndexOf("/") == 0) {
                    path = "/";
                } else {
                    path = (path.substring(0, path.lastIndexOf("/")));
                }
                moveLabel.setText(path);
                moveFileBeanObservableList.clear();
                moveFileBeanObservableList.addAll(fileServe.getDirectory(path));
                moveTable.setItems(moveFileBeanObservableList);
            }
        });
        dialog.getDialogPane().setContent(borderPane);
        //获取当前目录
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == removeButtonType) {
                return moveLabel.getText();
            }
            return null;
        });
        //移动
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(path -> {
            String message = fileServe.move(moveFileBeanArrayList, path);
            if (Objects.nonNull(message)) {
                alert("移动失败", "error", message);
            }
            flush();
        });

    }

    /**
     * 搜索文件
     *
     * @param actionEvent actionEvent
     */
    public void getSearchFileItem(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("新建文件夹");
        dialog.setHeaderText("文件夹名");
        dialog.setContentText("请输入新的文件夹的名称：");

        AtomicReference<String> currentIdentity = new AtomicReference<>("");
        CheckBox checkBox = new CheckBox("仅搜索当前目录");
        checkBox.setSelected(true);
        dialog.getDialogPane().setExpandableContent(checkBox);

        Optional<String> result = dialog.showAndWait();

        //刷新
        result.ifPresent(name -> {
            if (checkBox.isSelected()) {
                currentIdentity.set(MD5.create().digestHex(label.getText()));
            }
            fileBeanObservableList.clear();
            fileBeanObservableList.addAll(fileServe.searchFile(currentIdentity.get(), name));
            table.setItems(fileBeanObservableList);
        });
    }

    /**
     * 刷新table
     *
     * @param actionEvent actionEvent
     */
    public void getFlushItem(ActionEvent actionEvent) {
        flush();
    }

    /**
     * 获取文件信息
     *
     * @param actionEvent actionEvent
     */
    public void getFileInfoItem(ActionEvent actionEvent) {
        StringBuffer stringBuffer = new StringBuffer();
        getSelectFileBeanArrayList().forEach(s -> {
            stringBuffer.append(s.toString());
            stringBuffer.append("\n");
        });
        String headerText = getSelectFileBeanArrayList().get(0).getName();
        String body = stringBuffer.toString();
        alert("文件信息", headerText, body);
    }

    /**
     * 返回基础对话框样式
     *
     * @param title       title
     * @param contentText contentText
     * @param headerText  headerText
     * @param body        body
     * @return Alert
     */
    private Alert alert(String title, String contentText, String headerText, String body) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(contentText);
        alert.setHeaderText(headerText);

        TextArea textArea = new TextArea(body);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane gridPane = new GridPane();
        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(gridPane);

        return alert;
    }

    /**
     * 显示对话框
     *
     * @param title      title
     * @param headerText headerText
     * @param body       body
     */
    private void alert(String title, String headerText, String body) {
        Alert alert = alert(title, null, headerText, body);
        alert.showAndWait();
    }

    /**
     * 删除文件
     *
     * @param actionEvent actionEvent
     */
    public void getDeleteItem(ActionEvent actionEvent) {
        ArrayList<FileBean> deleteFileBeanArrayList = getSelectFileBeanArrayList();
        //防止删除时卡顿
        new Thread(() -> {
            table.getItems().removeAll(deleteFileBeanArrayList);
            fileServe.delete(deleteFileBeanArrayList);
        }).start();
    }

    /**
     * 打开离线下载页面
     *
     * @param actionEvent
     */
    @SneakyThrows
    public void getAddOffLineItem(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/OffLineAddView.fxml"));
        Stage stages = new Stage();

        Parent root = loader.load();
        stages.setScene(new Scene(root));
        stages.setResizable(false);
        stages.show();

        stages.setTitle(label.getText() + "  " + fileServe.quota());


        OffLineAddView offLineAddView = loader.getController();
        offLineAddView.setToken(token);
        offLineAddView.setExistTable(existOffLineTable);
        offLineAddView.setPath(label.getText());

    }

    /**
     * 打开离线列表
     *
     * @param actionEvent actionEvent
     */
    @SneakyThrows
    public void getOffLineltem(ActionEvent actionEvent) {
        existOffLineTable = true;

        OffLineTable offLineTable = new OffLineTable(label.getText(), token);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/OffLineTable.fxml"));
        loader.setController(offLineTable);
        Parent root = loader.load();
        if (offLineTableStage == null) {
            offLineTableStage = new Stage();
            offLineTableStage.setScene(new Scene(root));
            offLineTableStage.setTitle("离线下载列表");
            offLineTableStage.show();
        } else {
            offLineTableStage.requestFocus();
        }

        offLineTableStage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, windowEvent -> {
            existOffLineTable = false;
            offLineTableStage = null;
        });

        offLineTable.table.setOnMouseClicked(event -> {
            OffLineBean selectedItem = offLineTable.table.getSelectionModel().getSelectedItem();
            if (event.getButton() == MouseButton.PRIMARY) {
                offLineTable.contextMenu.hide();
            }

            if (event.getClickCount() == 2) {
                //进入FistList文件夹
                if (selectedItem.isDirectory()) {
                    fileBeanObservableList.clear();
                    fileBeanObservableList.addAll(fileServe.getFileAll(selectedItem.getAccessPath()));
                    table.setItems(fileBeanObservableList);
                    label.setText(selectedItem.getAccessPath());
                    //FileList获取焦点
                    table.getScene().getWindow().requestFocus();
                }

                if (!selectedItem.isDirectory()) {
                    //视频浏览
                    if (selectedItem.getFileMime().contains("video")) {
                        openVideoView(selectedItem);
                    }
                }

            }
        });

    }

    /**
     * 将下载链接输出到系统剪贴板
     *
     * @param actionEvent actionEvent
     */
    public void getDownloadItem(ActionEvent actionEvent) {
        ArrayList<FileBean> fileBeanArrayList = getSelectFileBeanArrayList();

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("获取下载链接");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
        Label label = new Label("获取下载链接中。。。");
        label.setMaxWidth(Double.MAX_VALUE);
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(label);
        borderPane.setCenter(progressBar);
        dialog.getDialogPane().setContent(borderPane);


        Service<Integer> service = new Service<>() {
            @Override
            protected Task<Integer> createTask() {
                return new Task<>() {
                    @Override
                    protected Integer call() {
                        String downloadUrl;
                        if (fileBeanArrayList.size() == 1 && !fileBeanArrayList.get(0).isDirectory()) {
                            downloadUrl = fileServe.download(fileBeanArrayList.get(0));
                        } else {
                            downloadUrl = fileServe.download(fileBeanArrayList);
                        }

                        Platform.runLater(dialog::close);
                        if (Objects.nonNull(downloadUrl)) {
                            ClipBoardUtil.setClipboardString(downloadUrl);
                        } else {
                            Platform.runLater(() -> {
                                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                errorAlert.setTitle("下载失败");
                                errorAlert.setContentText("文件总大小超过限制");
                                errorAlert.show();
                            });
                        }

                        return null;
                    }
                };
            }
        };
        service.start();
        progressBar.progressProperty().bind(service.progressProperty());
        dialog.show();
        //取消服务
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.CANCEL) {
                service.cancel();
            }
            return null;
        });

    }

    /**
     * 强制播放视频
     *
     * @param actionEvent actionEvent
     */
    public void forcePlay(ActionEvent actionEvent) {
        openVideoView(getSelectFileBeanArrayList().get(0));
    }

    /**
     * 读取文件,使用系统默认的编码读取
     *
     * @param actionEvent
     */
    public void readText(ActionEvent actionEvent) {
        FileBean selectedItem = table.getSelectionModel().getSelectedItem();
        byte[] content = "不能读取文件夹".getBytes(Charset.defaultCharset());
        if (!selectedItem.isDirectory()) {
            //文件大小小于10m
            if (selectedItem.getSize() <= 10485760) {
                String downloadUrl = fileServe.download(selectedItem);
                content = HttpUtil.downloadBytes(downloadUrl);
            } else {
                content = "文件太大啦~，下载后在好好看吧o(>﹏<)o".getBytes(Charset.defaultCharset());
            }
        }
        Alert alert = alert(selectedItem.getName(), null, "文件内容", new String(content, Charset.defaultCharset()));
        HBox hBox = new HBox();
        Label label = new Label("文件编码: ");
        TextField encodeTextField = new TextField(Charset.defaultCharset().toString());


        hBox.getChildren().addAll(label, encodeTextField);
        hBox.setAlignment(Pos.CENTER);

        GridPane gridPane = (GridPane) alert.getDialogPane().getExpandableContent();
        TextArea textArea = (TextArea) gridPane.getChildren().get(0);

        gridPane.add(hBox, 0, 1);


        byte[] finalContent = content;
        encodeTextField.setOnKeyReleased(e -> {
            try {
                textArea.setText(new String(finalContent, encodeTextField.getText()));
            } catch (UnsupportedEncodingException ignored) {
            }
        });
        encodeTextField.setOnMouseClicked(e -> {
            encodeTextField.setText("");
        });
        alert.showAndWait();
    }

    /**
     * 退出登录
     *
     * @param actionEvent actionEvent
     */
    public void exitLogin(ActionEvent actionEvent) {
        Stage stage = (Stage) table.getScene().getWindow();
        AutoLogin autoLogin = new AutoLogin();
        autoLogin.login(stage);
    }

    /**
     * 进入下一级，返回上一级，
     *
     * @param e
     */
    public void tableMouseEvent(MouseEvent e) {
        //左键,进入下一级、打开视频、打开图片
        if (e.getButton() == MouseButton.PRIMARY) {
            tableClick(e, 2);
            contextMenu.hide();
        }
        //上一级
        if (e.getButton() == MouseButton.BACK) {
            back(e);
        }
        //前进键，进入下一级、打开视频、打开图片
        if (e.getButton() == MouseButton.FORWARD) {
            tableClick(e, 1);
        }

    }

    /**
     * table 按钮事件
     * F2重命名 delete 删除
     *
     * @param e KeyEvent
     */
    public void tableKey(KeyEvent e) {
        if (e.getCode() == KeyCode.F2) {
            editFileName();
        }
        if (e.getCode() == KeyCode.DELETE) {
            getDeleteItem(new ActionEvent());
        }
        if (e.getCode() == KeyCode.ENTER) {
            enterNextOrPlay();
        }
        if (e.isControlDown() & e.getCode() == KeyCode.F) {
            getSearchFileItem(new ActionEvent());
        }

    }

    /**
     * 下一级、打开视频、打开图片
     *
     * @param e          鼠标事件
     * @param clickCount 点击次数
     */
    @SneakyThrows
    private void tableClick(MouseEvent e, int clickCount) {
        if (e.getClickCount() == clickCount) {
            enterNextOrPlay();
        }
    }

    /**
     * 进入下一级/播放视频/打开图片/显示文件信息
     */
    private void enterNextOrPlay() {
        FileBean fileBean = table.getSelectionModel().getSelectedItem();
        //下一个文件
        if (Objects.nonNull(fileBean)) {
            if (fileBean.isDirectory()) {
                currentSelectionRow = table.getSelectionModel().getSelectedIndex();

                fileBeanObservableList.clear();
                fileBeanObservableList.addAll(fileServe.getFileAll(fileBean.getPath()));
                table.setItems(fileBeanObservableList);
                label.setText(fileBean.getPath());
            } else {
                if (fileBean.getMime().contains("image")) {
                    //图片浏览
                    openPictureView(fileBean, token);
                    return;
                }
                //视频浏览
                if (fileBean.getMime().contains("video") || fileBean.getName().contains(".mp4")) {
                    openVideoView(fileBean);
                    return;
                }
                //text
                if (fileBean.getMime().contains("text") || fileBean.getName().contains(".txt")) {
                    readText(new ActionEvent());
                    return;
                }
                //都不是就显示文件信息
                getFileInfoItem(new ActionEvent());
            }
        }
    }


    /**
     * 打开显示图片窗口
     *
     * @param fileBean 文件
     * @param token    token
     */
    private void openPictureView(FileBean fileBean, String token) {
        Stage stage1 = new Stage();
        PictureView pictureView = new PictureView(fileBean, token, table);
        pictureView.start(stage1);
    }

    /**
     * 打开显示视频窗口
     *
     * @param fileBean 文件
     */
    private void openVideoView(FileBean fileBean) {
        VideoView videoView = new VideoView(fileBean, fileServe);
        openVideoView(videoView);
    }

    /**
     * 打开显示视频窗口
     *
     * @param offLineBean 文件
     */
    private void openVideoView(OffLineBean offLineBean) {
        VideoView videoView = new VideoView(offLineBean, fileServe);
        openVideoView(videoView);
    }

    /**
     * play video
     *
     * @param videoView {@link VideoView}
     */
    private void openVideoView(VideoView videoView) {
        Stage stage1 = new Stage();
        videoView.start(stage1);
        stage1.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, b -> {
            videoView.embeddedMediaPlayer.controls().stop();
            videoView.embeddedMediaPlayer.release();
            videoView.mediaPlayerFactory.release();
        });
    }

    /**
     * 获取table选中的项目
     *
     * @return all select item
     */
    private ArrayList<FileBean> getSelectFileBeanArrayList() {
        return new ArrayList<>(table.getSelectionModel().getSelectedItems());
    }

    /**
     * 刷新table
     */
    private void flush() {
        fileBeanObservableList.clear();
        fileBeanObservableList.addAll(fileServe.getFileAll(label.getText()));
        table.setItems(fileBeanObservableList);
    }

    /**
     * 点击最上方的lable，返回上一级
     *
     * @param e
     */
    public void back(MouseEvent e) {
        if (e.getClickCount() == 1) {
            String path = label.getText();
            if (path.lastIndexOf("/") == 0) {
                path = "/";
            } else {
                path = (path.substring(0, path.lastIndexOf("/")));
            }
            fileBeanObservableList.clear();
            fileBeanObservableList.addAll(fileServe.getFileAll(path));
            table.setItems(fileBeanObservableList);
            label.setText(path);

            table.getSelectionModel().select(currentSelectionRow);
            table.scrollTo(currentSelectionRow);

        }
    }

    /**
     * 重命名文件
     */
    private void editFileName() {
        if (Objects.nonNull(table.getSelectionModel().getSelectedItem())) {
            table.setEditable(true);
            int selectedRowIndex = table.getSelectionModel().getSelectedIndex();
            table.edit(selectedRowIndex, table.getColumns().get(0));
            table.setEditable(false);
        }
    }

}
