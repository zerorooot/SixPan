package com.github.zerorooot.view;


import com.github.zerorooot.bean.OffLineBean;
import com.github.zerorooot.serve.FileServe;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @Author: zero
 * @Date: 2020/8/9 20:34
 */
@NoArgsConstructor
public class OffLineTableView extends Application {
    private String cookie;
    private String path;
    protected TableView<OffLineBean> table;


    public OffLineTableView( String path,String cookie) {
        this.cookie = cookie;
        this.path = path;
    }

    @Override
    public void start(Stage stage) throws Exception {
        BorderPane borderPane = new BorderPane();
        FileServe fileServe = new FileServe(cookie);
        TableView<OffLineBean> table = new TableView<>();
        this.table = table;
        ObservableList<OffLineBean> fileBeanObservableList = FXCollections.observableArrayList();

        MenuItem offLineAdd = new MenuItem("添加离线任务");
        MenuItem deleteComplete= new MenuItem("删除已完成任务");
        MenuItem flush = new MenuItem("刷新");
        MenuItem deleteCurrent = new MenuItem("删除选中任务");
        //添加离线任务
        offLineAdd.setOnAction(e->{
            try {
                Stage stages=new Stage();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/OffLineAddView.fxml"));
                Parent root = loader.load();
                stages.setScene(new Scene(root));
                stages.setResizable(false);
                stages.setTitle(path+"  "+fileServe.quota());
                stages.show();

                OffLineAddView offLineAddView = loader.getController();
                offLineAddView.setCookie(cookie);
                offLineAddView.setExistTable(true);
                offLineAddView.setPath(path);

                stages.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, windowEvent -> {
                    fileBeanObservableList.removeAll(fileBeanObservableList);
                    fileBeanObservableList.addAll(fileServe.getOffLine());
                    table.setItems(fileBeanObservableList);
                });
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        //删除已完成任务
        deleteComplete.setOnAction(e->{
            fileServe.deleteComplete();
            fileBeanObservableList.removeAll(fileBeanObservableList);
            fileBeanObservableList.addAll(fileServe.getOffLine());
            table.setItems(fileBeanObservableList);
        });

        //刷新
        flush.setOnAction(e->{
            fileBeanObservableList.removeAll(fileBeanObservableList);
            fileBeanObservableList.addAll(fileServe.getOffLine());
            table.setItems(fileBeanObservableList);
        });
        //删除选中任务
        deleteCurrent.setOnAction(e->{
            ArrayList<OffLineBean> deleteOffLineBeanArrayList = new ArrayList<>();
            table.getItems().forEach(s->{
                if (s.getCheckBox().isSelected()) {
                    deleteOffLineBeanArrayList.add(s);
                }
            });

            if (Objects.nonNull(table.getSelectionModel().getSelectedItem()) && deleteOffLineBeanArrayList.size() == 0) {
                OffLineBean fileBean = table.getSelectionModel().getSelectedItem();
                deleteOffLineBeanArrayList.add(fileBean);
            }
            fileServe.offLineDelete(deleteOffLineBeanArrayList);
            table.getItems().removeAll(deleteOffLineBeanArrayList);
        });


        ContextMenu menu = new ContextMenu(offLineAdd, deleteComplete, deleteCurrent, flush);
        table.setContextMenu(menu);

        TableColumn<OffLineBean,CheckBox>  checkBoxColumn = new TableColumn<>();
        checkBoxColumn.setCellValueFactory(cellData ->cellData.getValue().getCheckBox().getCheckBox());
        Label checkBoxColumnLable = new Label("多选");
        checkBoxColumnLable.setOnMouseClicked(e->{
            table.getItems().forEach(s->{
                s.getCheckBox().setSelect(!s.getCheckBox().isSelected());
            });
        });
        checkBoxColumn.setGraphic(checkBoxColumnLable);
        checkBoxColumn.setSortable(false);

        TableColumn<OffLineBean, String> fileNameColumn = new TableColumn<>("文件名称");
        TableColumn<OffLineBean, String> savePathColumn = new TableColumn<>("存储路径");
        TableColumn<OffLineBean, String> progressColumn = new TableColumn<>("下载进度");
        TableColumn<OffLineBean, String> creatTimeColumn = new TableColumn<>("创建时间");


        table.getColumns().addAll(checkBoxColumn,fileNameColumn, savePathColumn, progressColumn, creatTimeColumn);

        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        savePathColumn.setCellValueFactory(new PropertyValueFactory<>("savePath"));
        progressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        creatTimeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));


        fileBeanObservableList.addAll(fileServe.getOffLine());

        table.setItems(fileBeanObservableList);

        table.setColumnResizePolicy(resizeFeatures -> {
            checkBoxColumn.setPrefWidth(table.widthProperty().get()*0.07);
            fileNameColumn.setPrefWidth(table.widthProperty().get() * 0.6);
            savePathColumn.setPrefWidth(table.widthProperty().get() * 0.11);
            progressColumn.setPrefWidth(table.widthProperty().get() * 0.11);
            creatTimeColumn.setPrefWidth(table.widthProperty().get() * 0.11);
            return true;
        });


        borderPane.setCenter(table);


        stage.setTitle("离线下载");
        stage.setScene(new Scene(borderPane,600,400));
        stage.show();
    }

}
