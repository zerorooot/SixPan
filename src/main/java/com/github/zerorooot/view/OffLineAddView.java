package com.github.zerorooot.view;

import com.github.zerorooot.serve.FileServe;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.NoArgsConstructor;

/**
 * @Author: zero
 * @Date: 2020/8/9 19:17
 */
@NoArgsConstructor
public class OffLineAddView extends Application {
    @FXML
    public TextArea textArea;
    @FXML
    public TextField password;
    @FXML
    public Button addUrl;
    private String cookie;
    private String path;
    private boolean existTable;
    private FileServe fileServe;


    public OffLineAddView(String cookie, String path, boolean existTable) {
        this.cookie = cookie;
        this.path = path;
        this.existTable = existTable;
    }

    @Override
    public void init() throws Exception {
        FileServe fileServe = new FileServe(cookie);
        this.fileServe = fileServe;
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/OffLineAddView.fxml"));
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.show();
        Platform.runLater(() -> stage.setTitle("添加离线任务 " + new FileServe(cookie).quota()));


    }

    @FXML
    public void addUrl() {
        if ("".equals(password.getText())) {
            fileServe.addOffLine(path, textArea.getText(), null);
        }
        fileServe.addOffLine(path, textArea.getText(), password.getText());
        if (!existTable) {
            OffLineTableView offLineTableView = new OffLineTableView(path, cookie);
            Stage stages = new Stage();
            try {
                offLineTableView.start(stages);
                Stage primaryStage = (Stage) addUrl.getScene().getWindow();
                primaryStage.close();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

    }
}
