package com.github.zerorooot.view;

/**
 * @Author: zero
 * @Date: 2020/8/5 21:11
 */

import com.github.zerorooot.bean.FileBean;
import com.github.zerorooot.bean.TokenBean;
import com.github.zerorooot.serve.FileServe;
import com.github.zerorooot.serve.LoginServe;
import com.github.zerorooot.util.PropertiesUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;


public class LoginView extends Application {
    private Button loginButton;
    private TextField accountField;
    private PasswordField passwordField;


    @Override
    public void init() throws Exception {
        this.accountField = new TextField();
        this.passwordField = new PasswordField();
        this.loginButton = new Button("登录");

        Properties properties = PropertiesUtil.getProperties();
        String account = properties.getProperty("account");
        String password = properties.getProperty("password");
        if (Objects.nonNull(account) && Objects.nonNull(password)) {
            accountField.setText(account);
            passwordField.setText(password);

        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        StackPane root = new StackPane();

        AnchorPane anchorPane = new AnchorPane();
        anchorPane.prefHeight(178);
        anchorPane.prefWidth(312);


        Label accountLable = new Label("账号");
        accountLable.setLayoutX(53);
        accountLable.setLayoutY(42);

        accountField.setLayoutX(95);
        accountField.setLayoutY(38);


        Label paccwordLable = new Label("密码");
        paccwordLable.setLayoutX(53);
        paccwordLable.setLayoutY(83);

        passwordField.setLayoutX(95);
        passwordField.setLayoutY(79);


        loginButton.setLayoutX(135);
        loginButton.setLayoutY(125);

        loginButton.setOnMouseClicked(e -> {
            login(anchorPane);
        });

        anchorPane.getChildren().addAll(accountLable, accountField, paccwordLable, passwordField, loginButton);

        root.getChildren().add(anchorPane);
        if (!"".equals(accountField.getText())) {
            Platform.runLater(() -> loginButton.requestFocus());
        }


        primaryStage.setTitle("登录");
        primaryStage.setScene(new Scene(root, 312, 178));
        primaryStage.setResizable(false);
        primaryStage.show();


    }


    public void login(AnchorPane anchorPane) {
        String account = accountField.getText();
        String password = passwordField.getText();
        LoginServe loginServe = new LoginServe();
        TokenBean tokenBean = loginServe.login(account, password);
        String cookie = tokenBean.getCookie();
        if (Objects.nonNull(cookie)) {
            try {
                Properties properties = PropertiesUtil.getProperties();
                properties.setProperty("account", account);
                properties.setProperty("password", password);
                PropertiesUtil.setProperties(properties);
                Stage primaryStage = (Stage) anchorPane.getScene().getWindow();
                primaryStage.close();
                FileView fileView = new FileView(cookie);
                Stage stage = new Stage();
                fileView.start(stage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
