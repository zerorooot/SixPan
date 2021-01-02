package com.github.zerorooot.view;

import com.github.zerorooot.bean.TokenBean;
import com.github.zerorooot.serve.LoginServe;
import com.github.zerorooot.util.PropertiesUtil;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.SneakyThrows;

import java.net.URL;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * @Author: zero
 * @Date: 2020/8/19 20:53
 * TODO
 * loding 动画
 */
public class Login implements Initializable {
    public Button loginButton;
    public TextField accountField;
    public TextField passwordField;

    @SneakyThrows
    public void login(MouseEvent event) {
        String account = accountField.getText();
        String password = passwordField.getText();
        LoginServe loginServe = new LoginServe();
        TokenBean tokenBean = loginServe.login(account, password);
        String token = tokenBean.getToken();
        if (Objects.nonNull(token)) {
            Properties properties = PropertiesUtil.getProperties();
            properties.setProperty("account", account);
            properties.setProperty("password", password);
            PropertiesUtil.setProperties(properties);

            Stage stages = new Stage();
            FileList fileList = new FileList(token);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FileList.fxml"));
            loader.setController(fileList);

            Parent root = loader.load();
            stages.setScene(new Scene(root));
            stages.show();

            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();
        }else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("登录失败");
            alert.setHeaderText(null);
            alert.setContentText("用户名或账号错误");

            alert.showAndWait();
        }

    }

    @SneakyThrows
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loginButton.requestFocus();
        Properties properties = PropertiesUtil.getProperties();
        String account = properties.getProperty("account");
        String password = properties.getProperty("password");
        if (Objects.nonNull(account) && Objects.nonNull(password)) {
            accountField.setText(account);
            passwordField.setText(password);
        }
    }


}
