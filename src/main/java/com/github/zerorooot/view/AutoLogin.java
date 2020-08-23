package com.github.zerorooot.view;

import com.github.zerorooot.AppMain;
import com.github.zerorooot.bean.TokenBean;
import com.github.zerorooot.serve.LoginServe;
import com.github.zerorooot.util.PropertiesUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.Properties;

/**
 * @Author: zero
 * @Date: 2020/8/20 20:05
 */
public class AutoLogin extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Properties properties = PropertiesUtil.getProperties();
        String account = properties.getProperty("account");
        String password = properties.getProperty("password");
        if (Objects.nonNull(account) && Objects.nonNull(password)) {
            LoginServe loginServe = new LoginServe();
            TokenBean tokenBean = loginServe.login(account, password);
            String cookie = tokenBean.getCookie();
            if (Objects.nonNull(cookie)) {
                fileList(primaryStage, cookie);
            }else {
                login(primaryStage);
            }
        }else {
            login(primaryStage);
        }
    }

    @SneakyThrows
    public void login(Stage primaryStage) {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Login.fxml")));
        Scene scene = new Scene(root);
        primaryStage.setTitle("登录");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @SneakyThrows
    public void fileList(Stage primaryStage,String cookie) {
        FileList fileList = new FileList(cookie);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FileList.fxml"));
        loader.setController(fileList);

        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

    }

}
