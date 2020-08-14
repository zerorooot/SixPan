package com.github.zerorooot.view;


import com.github.zerorooot.bean.OffLineBean;
import com.github.zerorooot.serve.FileServe;
import javafx.collections.FXCollections;
import com.github.zerorooot.bean.FileBean;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javafx.stage.WindowEvent;
import javafx.util.Callback;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;


/**
 * @Author: zero
 * @Date: 2020/8/6 21:50
 */
@NoArgsConstructor
public class FileView extends Application {
    private String cookie;
    private boolean existTable = false;
    private Label label;
    private TableView<FileBean> table;
    private FileServe fileServe;
    private ObservableList<FileBean> fileBeanObservableList = FXCollections.observableArrayList();

    public FileView(String cookie) {
        this.cookie = cookie;
    }


    @Override
    public void start(Stage stage) throws Exception {
        label = new Label("/");
        label.setMaxWidth(Double.MAX_VALUE);

        table = new TableView<>();
        fileServe = new FileServe(cookie);

        TableColumn<FileBean, CheckBox> checkBoxColumn = new TableColumn<>();
        checkBoxColumn.setCellValueFactory(cellData -> cellData.getValue().getCheckBox().getCheckBox());
        Label checkBoxColumnLable = new Label("全选");
        checkBoxColumnLable.setOnMouseClicked(e -> {
            table.getItems().forEach(s -> {
                s.getCheckBox().setSelect(!s.getCheckBox().isSelected());
            });
        });
        checkBoxColumn.setGraphic(checkBoxColumnLable);
        checkBoxColumn.setSortable(false);


        TableColumn<FileBean, String> nameTableColumn = new TableColumn<>("名称");
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameTableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameTableColumn.setEditable(true);
        //重命名
        nameTableColumn.setOnEditCommit(
                t -> {
                    FileBean fileBean = t.getRowValue();
                    (t.getTableView().getItems().get(t.getTablePosition().getRow())).setName(t.getNewValue());
                    fileServe.rename(fileBean, t.getNewValue());
                }
        );

        TableColumn<FileBean, Long> sizeTableColumn = new TableColumn<>("大小");
        sizeTableColumn.setCellValueFactory(new PropertyValueFactory<>("size"));

        TableColumn<FileBean, String> createTimeColumn = new TableColumn<>("创建时间");
        createTimeColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));

        //重新命名
        MenuItem editItem = getEditItem();
        //新建文件
        MenuItem createFolderItem = getCreateFolderItem();
        //移动文件
        MenuItem moveFileItem = getMoveFileItem();
        //刷新文件
        MenuItem flushItem = getFlushItem();
        //删除文件
        MenuItem deleteItem = getDeleteItem();
        //离线下载
        MenuItem addOffLineItem = getAddOffLineItem();
        //离线列表
        MenuItem offLineViewltem = getOffLineViewltem();
        ContextMenu menu = new ContextMenu(editItem, createFolderItem, moveFileItem, flushItem, deleteItem, addOffLineItem,
                offLineViewltem);
        table.setContextMenu(menu);


        //设置column大小
        table.setColumnResizePolicy(resizeFeatures -> {
            checkBoxColumn.setPrefWidth(table.widthProperty().get() * 0.03);
            nameTableColumn.setPrefWidth(table.widthProperty().get() * 0.7);
            sizeTableColumn.setPrefWidth(table.widthProperty().get() * 0.126);
            createTimeColumn.setPrefWidth(table.widthProperty().get() * 0.126);
            return true;
        });


        table.getColumns().addAll(checkBoxColumn, nameTableColumn, sizeTableColumn, createTimeColumn);

        ArrayList<FileBean> fileBeanArrayList = fileServe.getFileAll("/");
        fileBeanObservableList.addAll(fileBeanArrayList);


        table.setItems(fileBeanObservableList);

        //下一级、打开视频、打开图片
        table.setOnMouseClicked(e -> {
            tableClick(e, 2);
        });

        //返回上一级
        label.setOnMouseClicked(this::back);

        BorderPane root = new BorderPane();
        root.setTop(label);
        root.setCenter(table);
        stage.setScene(new Scene(root, 1280, 720));
        stage.setResizable(true);
        stage.show();

        //f2编辑
        table.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.F2) {
                editItem.fire();
            }
        });
        table.setOnMouseReleased(e -> {
            //上一级
            if (e.getButton() == MouseButton.BACK) {
                back(e);
            }
            //前进
            if (e.getButton() == MouseButton.FORWARD) {
                tableClick(e, 1);
            }
        });
    }

    /**
     * 返回上一级
     *
     * @param e 鼠标事件
     */
    private void back(MouseEvent e) {
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
        }
    }

    /**
     * 下一级、打开视频、打开图片
     *
     * @param e          鼠标事件
     * @param clickCount 点击次数
     */
    private void tableClick(MouseEvent e, int clickCount) {
        if (e.getClickCount() == clickCount) {
            FileBean fileBean = table.getSelectionModel().getSelectedItem();
            //下一个文件
            if (fileBean.isDirectory()) {
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

    /**
     * 重命名某文件
     *
     * @return 重命名菜单
     */
    private MenuItem getEditItem() {
        MenuItem editItem = new MenuItem("重新命名");
        editItem.setOnAction(e -> {
            if (Objects.nonNull(table.getSelectionModel().getSelectedItem())) {
                table.setEditable(true);
                int selectedRowIndex = table.getSelectionModel().getSelectedIndex();
                table.edit(selectedRowIndex, table.getColumns().get(1));
                table.setEditable(false);
            }
        });
        return editItem;
    }

    /**
     * 创建新文件
     *
     * @return 创建新文件菜单
     */
    private MenuItem getCreateFolderItem() {
        MenuItem createFolderItem = new MenuItem("新建文件");
        createFolderItem.setOnAction(e -> {
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
        });
        return createFolderItem;
    }


    private MenuItem getMoveFileItem() {
        MenuItem moveFileItem = new MenuItem("移动文件");
        moveFileItem.setOnAction(e -> {
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
                getFlushItem().fire();

            });

        });
        return moveFileItem;
    }

    /**
     * 刷新table
     *
     * @return 刷新菜单
     */
    private MenuItem getFlushItem() {
        MenuItem flushItem = new MenuItem("刷新文件");
        flushItem.setOnAction(e -> {
            fileBeanObservableList.removeAll(fileBeanObservableList);
            fileBeanObservableList.addAll(fileServe.getFileAll(label.getText()));
            table.setItems(fileBeanObservableList);
        });
        return flushItem;
    }

    /**
     * 删除文件
     *
     * @return 删除菜单
     */
    private MenuItem getDeleteItem() {
        MenuItem deleteItem = new MenuItem("删除文件");
        deleteItem.setOnAction(e -> {
            ArrayList<FileBean> deleteFileBeanArrayList = getSelectFileBeanArrayList();

            fileServe.delete(deleteFileBeanArrayList);
            table.getItems().removeAll(deleteFileBeanArrayList);
        });
        return deleteItem;
    }

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
     * 打开离线下载页面
     *
     * @return 离线下载菜单
     */
    private MenuItem getAddOffLineItem() {
        MenuItem addOffLineItem = new MenuItem("离线下载");
        addOffLineItem.setOnAction(e -> {
            OffLineAddView offLineAddView = new OffLineAddView(label.getText(), cookie, existTable);
            Stage stages = new Stage();
            try {
                offLineAddView.start(stages);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        return addOffLineItem;
    }

    /**
     * 打开离线列表
     *
     * @return 离线列表菜单
     */
    private MenuItem getOffLineViewltem() {
        MenuItem offLineViewltem = new MenuItem("离线列表");
        offLineViewltem.setOnAction(e -> {
            existTable = true;
            OffLineTableView offLineTableView = new OffLineTableView(label.getText(), cookie);
            Stage stages = new Stage();
            try {
                offLineTableView.start(stages);
                stages.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, windowEvent -> existTable = false);

                offLineTableView.table.setOnMouseClicked(e1->{
                    OffLineBean selectedItem = offLineTableView.table.getSelectionModel().getSelectedItem();
                    if (e1.getClickCount() == 2) {
                        //进入文件夹
                        if (selectedItem.isDirectory()) {
                            fileBeanObservableList.removeAll(fileBeanObservableList);
                            fileBeanObservableList.addAll(fileServe.getFileAll(selectedItem.getAccessPath()));
                            table.setItems(fileBeanObservableList);
                            label.setText(selectedItem.getAccessPath());
                        //fileview获取焦点
                            table.getScene().getWindow().requestFocus();
                        }else {
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

            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        return offLineViewltem;
    }
}
