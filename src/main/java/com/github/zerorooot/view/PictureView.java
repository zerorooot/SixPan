package com.github.zerorooot.view;


import java.util.List;
import java.util.stream.Collectors;

import com.github.zerorooot.bean.FileBean;
import com.github.zerorooot.serve.FileServe;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;


/**
 * @Author: zero
 * @Date: 2020/8/10 19:30
 */

@NoArgsConstructor
public class PictureView extends Application {
    private FileBean fileBean;
    private String token;

    private int currentIndex = 0;
    private Service<Integer> service;

    public PictureView(FileBean fileBean, String token) {
        this.fileBean = fileBean;
        this.token = token;
    }

    public void start(Stage primaryStage) {
        ImageView imageView = new ImageView();
        FileServe fileServe = new FileServe(token);

        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER);
        Scene scene = new Scene(stackPane, 700, 500);
        ProgressIndicator progressIndicator = new ProgressIndicator();
        primaryStage.setScene(scene);

        service = new Service<>() {
            @Override
            protected Task<Integer> createTask() {
                return new Task<>() {
                    @SneakyThrows
                    @Override
                    protected Integer call() {
                        Platform.runLater(() -> {
                            stackPane.getChildren().removeAll(stackPane.getChildren());
                            stackPane.getChildren().add(progressIndicator);
                        });

                        Image image = new Image(fileServe.download(fileBean));
                        imageView.setImage(image);
                        double w = image.getWidth();
                        double h = image.getHeight();
                        final double max = Math.max(w, h);
                        int width = (int) (500 * w / max);
                        final int height = (int) (500 * h / max);
                        imageView.setFitHeight(height);
                        imageView.setFitWidth(width);
                        imageView.setCache(true);
                        imageView.setPreserveRatio(true);

                        Platform.runLater(() -> {
                            stackPane.getChildren().removeAll(stackPane.getChildren());
                            stackPane.getChildren().add(imageView);
                        });

                        return null;
                    }
                };
            }
        };

        primaryStage.show();
        service.start();

        stackPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            imageView.setY((newValue.doubleValue() - imageView.getFitHeight()) / 2);
        });

        stackPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            imageView.setX((newValue.doubleValue() - imageView.getFitWidth()) / 2);
        });

        List<FileBean> pictureArrayList =
                fileServe.getNonDirectory(fileBean.getParentPath()).stream().filter(e -> e.getMime().contains("image")).collect(Collectors.toList());

        for (int i = 0; i < pictureArrayList.size(); i++) {
            if (fileBean.getPath().equals(pictureArrayList.get(i).getPath())) {
                currentIndex = i;
                break;
            }
        }

        scene.setOnKeyPressed(keyEvent -> {
            //上一张
            if (keyEvent.getCode() == KeyCode.LEFT && currentIndex > 0) {
                currentIndex = currentIndex - 1;
                FileBean currentFileBean = pictureArrayList.get(currentIndex);
                primaryStage.setTitle(currentFileBean.getName() + "  " + (currentIndex + 1) + "/" + pictureArrayList.size());

                this.fileBean = currentFileBean;
                service.restart();
            }
            //下一张
            if (keyEvent.getCode() == KeyCode.RIGHT && currentIndex + 1 < pictureArrayList.size()) {
                currentIndex = currentIndex + 1;
                FileBean currentFileBean = pictureArrayList.get(currentIndex);
                primaryStage.setTitle(currentFileBean.getName() + "  " + (currentIndex + 1) + "/" + pictureArrayList.size());

                this.fileBean = currentFileBean;
                service.restart();
            }
        });

        primaryStage.setTitle(fileBean.getName() + "  " + (currentIndex + 1) + "/" + pictureArrayList.size());

    }
}
