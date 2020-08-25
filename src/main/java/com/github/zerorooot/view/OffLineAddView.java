package com.github.zerorooot.view;

import com.github.zerorooot.serve.FileServe;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

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
    private String token;
    private String path;
    private boolean existTable;
    private Stage addOffLineStage;



    @Override
    public void start(Stage stage) {}

    @FXML
    @SneakyThrows
    public void addUrl() {
        FileServe fileServe = new FileServe(token);
        if ("".equals(password.getText())) {
            fileServe.addOffLine(path, textArea.getText().replaceAll(" ", ""), null);
        }
        fileServe.addOffLine(path, textArea.getText(), password.getText());
        if (!existTable) {
            Stage stages = new Stage();
            OffLineTable offLineTable = new OffLineTable(path, token);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/OffLineTable.fxml"));
            loader.setController(offLineTable);

            Parent root = loader.load();
            stages.setScene(new Scene(root));
            stages.setTitle("离线下载列表");
            stages.show();
        }
        Stage primaryStage = (Stage) addUrl.getScene().getWindow();
        primaryStage.close();
        addOffLineStage = null;

    }
}
