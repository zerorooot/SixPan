package com.github.zerorooot.view;

import com.github.zerorooot.bean.OffLineBean;
import com.github.zerorooot.serve.FileServe;
import com.github.zerorooot.util.ClipBoardUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @Author: zero
 * @Date: 2020/8/20 19:16
 */
@NoArgsConstructor
public class OffLineTable implements Initializable {
    public TableColumn<OffLineBean, String> fileNameColumn;
    public TableColumn<OffLineBean, String> savePathColumn;
    public TableColumn<OffLineBean, String> progressColumn;
    public TableColumn<OffLineBean, String> creatTimeColumn;
    public TableView<OffLineBean> table;
    public ContextMenu contextMenu;
    private final ObservableList<OffLineBean> fileBeanObservableList = FXCollections.observableArrayList();
    private FileServe fileServe;
    private String token;
    private String path;

    public OffLineTable(String path, String token) {
        this.token = token;
        this.path = path;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fileServe = new FileServe(token);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

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
            fileNameColumn.setPrefWidth(table.widthProperty().get() * 0.67);
            savePathColumn.setPrefWidth(table.widthProperty().get() * 0.1);
            progressColumn.setPrefWidth(table.widthProperty().get() * 0.1);
            creatTimeColumn.setPrefWidth(table.widthProperty().get() * 0.1);
            return true;
        });

    }

    /**
     * 添加离线任务
     *
     * @param actionEvent actionEvent
     */
    public void offLineAdd(ActionEvent actionEvent) {
        try {
            Stage stages = new Stage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OffLineAddView.fxml"));
            Parent root = loader.load();
            stages.setScene(new Scene(root));
            stages.setResizable(false);
            stages.setTitle(path + "  " + fileServe.quota());
            stages.show();

            OffLineAddView offLineAddView = loader.getController();
            offLineAddView.setToken(token);
            offLineAddView.setExistTable(true);
            offLineAddView.setPath(path);

            stages.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, windowEvent -> {
                fileBeanObservableList.clear();
                fileBeanObservableList.addAll(fileServe.getOffLine());
                table.setItems(fileBeanObservableList);
            });
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * 删除完成任务
     *
     * @param actionEvent
     */
    public void deleteComplete(ActionEvent actionEvent) {
        fileServe.deleteComplete();
        fileBeanObservableList.clear();
        fileBeanObservableList.addAll(fileServe.getOffLine());
        table.setItems(fileBeanObservableList);
    }

    /**
     * 刷新离线列表
     *
     * @param actionEvent
     */
    public void flush(ActionEvent actionEvent) {
        fileBeanObservableList.clear();
        fileBeanObservableList.addAll(fileServe.getOffLine());
        table.setItems(fileBeanObservableList);
    }

    /**
     * 删除选中任务
     *
     * @param actionEvent actionEvent
     */
    public void deleteCurrent(ActionEvent actionEvent) {
        ArrayList<OffLineBean> deleteOffLineBeanArrayList =new ArrayList<>(table.getSelectionModel().getSelectedItems());
        fileServe.offLineDelete(deleteOffLineBeanArrayList);
        table.getItems().removeAll(deleteOffLineBeanArrayList);
    }

    /**
     * 获取任务详情
     *
     * @param actionEvent actionEvent
     */
    public void detail(ActionEvent actionEvent) {
        OffLineBean selectedItem = table.getSelectionModel().getSelectedItem();
        if (Objects.nonNull(selectedItem)) {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("任务详情");
            ButtonType copyButtonType = new ButtonType("复制到剪贴板", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(copyButtonType, ButtonType.CANCEL);
            Label label = new Label("任务信息");
            label.setMaxWidth(Double.MAX_VALUE);
            TextArea textArea = new TextArea(selectedItem.getTextLink());
            BorderPane borderPane = new BorderPane();
            borderPane.setTop(label);
            borderPane.setCenter(textArea);
            dialog.getDialogPane().setContent(borderPane);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == copyButtonType) {
                    return textArea.getText();
                }
                return null;
            });
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(ClipBoardUtil::setClipboardString);
        }
    }

}
