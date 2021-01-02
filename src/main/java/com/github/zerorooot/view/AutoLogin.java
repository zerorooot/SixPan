package com.github.zerorooot.view;

import com.github.zerorooot.bean.TokenBean;
import com.github.zerorooot.serve.LoginServe;
import com.github.zerorooot.util.PropertiesUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * @Author: zero
 * @Date: 2020/8/20 20:05
 */
public class AutoLogin extends Preloader {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Login login = new Login();
        login.loadingAnime(primaryStage);

        new Thread(() -> {
            load(primaryStage);
        }).start();

    }

    @SneakyThrows
    private void load(Stage primaryStage) {
        Properties properties = PropertiesUtil.getProperties();
        String account = properties.getProperty("account");
        String password = properties.getProperty("password");
        if (Objects.nonNull(account) && Objects.nonNull(password)) {
            LoginServe loginServe = new LoginServe();
            TokenBean tokenBean = loginServe.login(account, password);
            String cookie = tokenBean.getToken();
            if (Objects.nonNull(cookie)) {
                Platform.runLater(() -> {
                    fileList(primaryStage, cookie);
                });
            } else {
                Platform.runLater(() -> {
                    login(primaryStage);
                });
            }
        } else {
            Platform.runLater(() -> {
                login(primaryStage);
            });
        }
    }

    @SneakyThrows
    public void login(Stage primaryStage) {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Login.fxml")));
        Scene scene = new Scene(root);
        primaryStage.setTitle("登录");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    @SneakyThrows
    public void fileList(Stage primaryStage, String cookie) {
        FileList fileList = new FileList(cookie);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FileList.fxml"));
        loader.setController(fileList);

        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.centerOnScreen();
        primaryStage.setTitle("");
        primaryStage.show();
    }

}
