package com.github.zerorooot.view;

import com.github.zerorooot.bean.FileBean;
import com.github.zerorooot.bean.OffLineBean;
import com.github.zerorooot.serve.FileServe;
import com.github.zerorooot.util.ClipBoardUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @Author: zero
 * @Date: 2020/8/19 21:13
 */
@NoArgsConstructor
public class FileList implements Initializable {
    public TableColumn<FileBean, CheckBox> checkBoxColumn;
    public TableColumn<FileBean, String> nameTableColumn;
    public TableColumn<FileBean, String> sizeTableColumn;
    public TableColumn<FileBean, String> createTimeColumn;
    public TableView<FileBean> table;
    public Label label;
    public ContextMenu contextMenu;
    private String cookie;
    private FileServe fileServe;
    private final ObservableList<FileBean> fileBeanObservableList = FXCollections.observableArrayList();
    private boolean existOffLineTable;
    private int currentSelectionRow;
    private Stage offLineTableStage;

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public FileList(String cookie) {
        this.cookie = cookie;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fileServe = new FileServe(cookie);

        //设置column大小
        table.setColumnResizePolicy(resizeFeatures -> {
            checkBoxColumn.setPrefWidth(table.widthProperty().get() * 0.03);
            nameTableColumn.setPrefWidth(table.widthProperty().get() * 0.7);
            sizeTableColumn.setPrefWidth(table.widthProperty().get() * 0.126);
            createTimeColumn.setPrefWidth(table.widthProperty().get() * 0.126);
            return true;
        });
        //设置column数值
        checkBoxColumn.setCellValueFactory(cellData -> cellData.getValue().getCheckBox().getCheckBox());
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameTableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameTableColumn.setEditable(true);
        sizeTableColumn.setCellValueFactory(new PropertyValueFactory<>("sizeString"));
        createTimeColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));

        //设置全选按钮
        Label checkBoxColumnLable = new Label("全选");
        checkBoxColumnLable.setOnMouseClicked(e -> {
            table.getItems().forEach(s -> {
                s.getCheckBox().setSelect(!s.getCheckBox().isSelected());
            });
        });
        checkBoxColumn.setGraphic(checkBoxColumnLable);
        checkBoxColumn.setSortable(false);

        //输出数值
        ArrayList<FileBean> fileBeanArrayList = fileServe.getFileAll("/");
        fileBeanObservableList.addAll(fileBeanArrayList);
        table.setItems(fileBeanObservableList);

        //设置右键菜单
        table.setOnContextMenuRequested(event -> {
            contextMenu.show(table, event.getScreenX(), event.getScreenY());
        });


    }

    /**
     * Column重命名
     *
     * @param t
     */
    public void rename(TableColumn.CellEditEvent<FileBean, String> t) {
        FileBean fileBean = t.getRowValue();
        (t.getTableView().getItems().get(t.getTablePosition().getRow())).setName(t.getNewValue());
        fileServe.rename(fileBean, t.getNewValue());
    }

    /**
     * 重命名某文件
     *
     * @param actionEvent
     */
    public void getEditItem(ActionEvent actionEvent) {
        editFileName();
    }

    /**
     * 创建新文件
     *
     * @param actionEvent
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
            fileBeanObservableList.removeAll(fileBeanObservableList);
            fileBeanObservableList.addAll(fileServe.getFileAll(label.getText()));
            table.setItems(fileBeanObservableList);
        });
    }

    /**
     * 移动文件
     *
     * @param actionEvent
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
        ArrayList<FileBean> directoryFileBeanArrayList = fileServe.getDirectory("/");
        moveFileBeanObservableList.addAll(directoryFileBeanArrayList);
        moveTable.setItems(moveFileBeanObservableList);

        //双击进入下一级
        moveTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                FileBean fileBean = moveTable.getSelectionModel().getSelectedItem();
                moveFileBeanObservableList.removeAll(moveFileBeanObservableList);
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
                moveFileBeanObservableList.removeAll(moveFileBeanObservableList);
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
            fileServe.move(moveFileBeanArrayList, path);
            flush();
        });

    }

    /**
     * 搜索文件
     *
     * @param actionEvent
     */
    public void getSearchFileItem(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("新建文件夹");
        dialog.setHeaderText("文件夹名");
        dialog.setContentText("请输入新的文件夹的名称：");
        Optional<String> result = dialog.showAndWait();

        //刷新
        result.ifPresent(name -> {
            fileBeanObservableList.removeAll(fileBeanObservableList);
            fileBeanObservableList.addAll(fileServe.searchFile(name));
            table.setItems(fileBeanObservableList);
        });
    }

    /**
     * 刷新table
     *
     * @param actionEvent
     */
    public void getFlushItem(ActionEvent actionEvent) {
        flush();
    }

    /**
     * 获取文件信息
     *
     * @param actionEvent
     */
    public void getFileInfoItem(ActionEvent actionEvent) {
        StringBuffer stringBuffer = new StringBuffer();
        getSelectFileBeanArrayList().forEach(s -> {
            stringBuffer.append(s.toString());
            stringBuffer.append("\n");
        });

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("文件信息");
        alert.setHeaderText(getSelectFileBeanArrayList().get(0).getName());

        String fileInfo = stringBuffer.toString();


        TextArea textArea = new TextArea(fileInfo);
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

        alert.showAndWait();
    }

    /**
     * 删除文件
     *
     * @param actionEvent
     */
    public void getDeleteItem(ActionEvent actionEvent) {
        ArrayList<FileBean> deleteFileBeanArrayList = getSelectFileBeanArrayList();

        fileServe.delete(deleteFileBeanArrayList);
        table.getItems().removeAll(deleteFileBeanArrayList);
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
        offLineAddView.setCookie(cookie);
        offLineAddView.setExistTable(existOffLineTable);
        offLineAddView.setPath(label.getText());

    }

    /**
     * 打开离线列表
     *
     * @param actionEvent
     */
    @SneakyThrows
    public void getOffLineltem(ActionEvent actionEvent) {
        existOffLineTable = true;

        OffLineTable offLineTable = new OffLineTable(label.getText(), cookie);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/OffLineTable.fxml"));
        loader.setController(offLineTable);
        Parent root = loader.load();
        if (offLineTableStage == null) {
            offLineTableStage = new Stage();
            offLineTableStage.setScene(new Scene(root));
            offLineTableStage.setTitle("离线下载列表");
            offLineTableStage.show();
        }else {
            offLineTableStage.requestFocus();
        }

        offLineTableStage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, windowEvent -> {
            existOffLineTable = false;
            offLineTableStage = null;
        });

        offLineTable.table.setOnMouseClicked(event -> {
            OffLineBean selectedItem = offLineTable.table.getSelectionModel().getSelectedItem();
            offLineTable.contextMenu.hide();
            if (event.getClickCount() == 2) {
                //进入FistList文件夹
                if (selectedItem.isDirectory()) {
                    fileBeanObservableList.removeAll(fileBeanObservableList);
                    fileBeanObservableList.addAll(fileServe.getFileAll(selectedItem.getAccessPath()));
                    table.setItems(fileBeanObservableList);
                    label.setText(selectedItem.getAccessPath());
                    //FileList获取焦点
                    table.getScene().getWindow().requestFocus();
                }

                if (!selectedItem.isDirectory()) {
                    //视频浏览
                    if (selectedItem.getFileMime().contains("video")) {
                        String download = fileServe.download(selectedItem);
                        VideoView videoView = new VideoView(download, selectedItem.getName());
                        Stage stage1 = new Stage();
                        videoView.start(stage1);
                        stage1.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, b -> {
                            videoView.embeddedMediaPlayer.controls().stop();
                            videoView.embeddedMediaPlayer.release();
                            videoView.mediaPlayerFactory.release();
                        });
                    }
                }

            }
        });

    }

    /**
     * 将下载链接输出到系统剪贴板
     * @param actionEvent
     */
    public void getDownloadItem(ActionEvent actionEvent) {
        ArrayList<FileBean> fileBeanArrayList = getSelectFileBeanArrayList();
        String download = fileServe.download(fileBeanArrayList);
        ClipBoardUtil.setClipboardString(download);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("下载链接获取成功");
        alert.setHeaderText(null);
        alert.setContentText("下载链接已粘贴到系统剪贴板，3秒后自动关闭\n(注：无法下载文件夹)");
        alert.show();

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(2000);
                if (alert.isShowing()) {
                    Platform.runLater(alert::close);
                }
            } catch (Exception exp) {
                exp.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 进入下一级，返回上一级，
     *
     * @param e
     */
    public void tableMouseEvent(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            tableClick(e, 2);
        }
        //上一级
        if (e.getButton() == MouseButton.BACK) {
            back(e);
        }
        //前进
        if (e.getButton() == MouseButton.FORWARD) {
            tableClick(e, 1);
        }
        contextMenu.hide();
    }

    /**
     * table 按钮事件
     *
     * @param e
     */
    public void tableKey(KeyEvent e) {
        if (e.getCode() == KeyCode.F2) {
            editFileName();
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
            FileBean fileBean = table.getSelectionModel().getSelectedItem();
            //下一个文件
            if (Objects.nonNull(fileBean)) {
                if (fileBean.isDirectory()) {
                    currentSelectionRow = table.getSelectionModel().getSelectedIndex();

                    fileBeanObservableList.removeAll(fileBeanObservableList);
                    fileBeanObservableList.addAll(fileServe.getFileAll(fileBean.getPath()));
                    table.setItems(fileBeanObservableList);
                    label.setText(fileBean.getPath());
                } else {
                    if (fileBean.getMime().contains("image")) {
                        //图片浏览
                        Stage stage1 = new Stage();
                        PictureView pictureView = new PictureView(fileBean, cookie);
                        pictureView.start(stage1);
                    }
                    //视频浏览
                    if (fileBean.getMime().contains("video")) {
                        Stage stage1 = new Stage();
                        String downloadUrl = fileServe.download(fileBean);
                        VideoView videoView = new VideoView(downloadUrl, fileBean.getName());
                        videoView.start(stage1);
                        stage1.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, b -> {
                            videoView.embeddedMediaPlayer.controls().stop();
                            videoView.embeddedMediaPlayer.release();
                            videoView.mediaPlayerFactory.release();
                        });
                    }
                }
            }
        }
    }

    /**
     * 获取table选中的项目
     *
     * @return
     */
    private ArrayList<FileBean> getSelectFileBeanArrayList() {
        ArrayList<FileBean> selectFileBeanArrayList = new ArrayList<>();
        table.getItems().forEach(s -> {
            if (s.getCheckBox().isSelected()) {
                selectFileBeanArrayList.add(s);
            }
        });

        if (Objects.nonNull(table.getSelectionModel().getSelectedItem()) && selectFileBeanArrayList.size() == 0) {
            FileBean fileBean = table.getSelectionModel().getSelectedItem();
            selectFileBeanArrayList.add(fileBean);
        }
        return selectFileBeanArrayList;
    }

    /**
     * 刷新table
     */
    private void flush() {
        fileBeanObservableList.removeAll(fileBeanObservableList);
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
            fileBeanObservableList.removeAll(fileBeanObservableList);
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
            table.edit(selectedRowIndex, table.getColumns().get(1));
            table.setEditable(false);
        }
    }

}
