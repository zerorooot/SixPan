package com.github.zerorooot.view;

import com.github.zerorooot.serve.FileServe;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @Author: zero
 * @Date: 2020/8/9 19:17
 */
@Setter
@NoArgsConstructor
public class OffLineAddView extends Application {
    @FXML
    public TextArea textArea;
    @FXML
    public TextField password;
    @FXML
    public Button addUrl;
    private String cookie ;
    private String path;
    private boolean existTable;



    @Override
    public void start(Stage stage) {}

    @FXML
    public void addUrl() {
        FileServe fileServe = new FileServe(cookie);
        if ("".equals(password.getText())) {
            fileServe.addOffLine(path, textArea.getText().replaceAll(" ", ""), null);
        }
        fileServe.addOffLine(path, textArea.getText(), password.getText());
        if (!existTable) {
            OffLineTableView offLineTableView = new OffLineTableView(path, cookie);
            Stage stages = new Stage();
            try {
                offLineTableView.start(stages);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        Stage primaryStage = (Stage) addUrl.getScene().getWindow();
        primaryStage.close();

    }
}
