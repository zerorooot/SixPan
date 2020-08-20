package com.github.zerorooot.view;

import com.github.zerorooot.bean.OffLineBean;
import com.github.zerorooot.serve.FileServe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * @Author: zero
 * @Date: 2020/8/20 19:16
 */
@NoArgsConstructor
public class OffLineTable implements Initializable {
    public TableColumn<OffLineBean, CheckBox> checkBoxColumn;
    public TableColumn<OffLineBean, String> fileNameColumn;
    public TableColumn<OffLineBean, String> savePathColumn;
    public TableColumn<OffLineBean, String> progressColumn;
    public TableColumn<OffLineBean, String> creatTimeColumn;
    public TableView<OffLineBean> table;
    public ContextMenu contextMenu;
    private final ObservableList<OffLineBean> fileBeanObservableList = FXCollections.observableArrayList();
    private FileServe fileServe;
    private String cookie;
    private String path;

    public OffLineTable(String cookie, String path) {
        this.cookie = cookie;
        this.path = path;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fileServe = new FileServe(cookie);


        //设置全选按钮
        checkBoxColumn.setCellValueFactory(cellData ->cellData.getValue().getCheckBox().getCheckBox());
        Label checkBoxColumnLable = new Label("多选");
        checkBoxColumnLable.setOnMouseClicked(e->{
            table.getItems().forEach(s->{
                s.getCheckBox().setSelect(!s.getCheckBox().isSelected());
            });
        });
        checkBoxColumn.setGraphic(checkBoxColumnLable);
        checkBoxColumn.setSortable(false);
        //设置表数据
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        savePathColumn.setCellValueFactory(new PropertyValueFactory<>("savePath"));
        progressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        creatTimeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

        //设置右键菜单
        table.setOnContextMenuRequested(event -> {
            contextMenu.show(table, event.getScreenX(), event.getScreenY());
        });

        //填充数据
        fileBeanObservableList.addAll(fileServe.getOffLine());
        table.setItems(fileBeanObservableList);

        //设置大小
        table.setColumnResizePolicy(resizeFeatures -> {
            checkBoxColumn.setPrefWidth(table.widthProperty().get()*0.07);
            fileNameColumn.setPrefWidth(table.widthProperty().get() * 0.6);
            savePathColumn.setPrefWidth(table.widthProperty().get() * 0.1);
            progressColumn.setPrefWidth(table.widthProperty().get() * 0.1);
            creatTimeColumn.setPrefWidth(table.widthProperty().get() * 0.1);
            return true;
        });

    }

    /**
     * 添加离线任务
     * @param actionEvent
     */
    public void offLineAdd(ActionEvent actionEvent) {
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
    }

    /**
     * 删除完成任务
     * @param actionEvent
     */
    public void deleteComplete(ActionEvent actionEvent) {
        fileServe.deleteComplete();
        fileBeanObservableList.removeAll(fileBeanObservableList);
        fileBeanObservableList.addAll(fileServe.getOffLine());
        table.setItems(fileBeanObservableList);
    }

    /**
     * 刷新离线列表
     * @param actionEvent
     */
    public void flush(ActionEvent actionEvent) {
        fileBeanObservableList.removeAll(fileBeanObservableList);
        fileBeanObservableList.addAll(fileServe.getOffLine());
        table.setItems(fileBeanObservableList);
    }

    /**
     * 删除选中任务
     * @param actionEvent
     */
    public void deleteCurrent(ActionEvent actionEvent) {
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
    }


}
