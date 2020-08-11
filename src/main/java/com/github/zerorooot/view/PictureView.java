package com.github.zerorooot.view;



import java.util.List;
import java.util.stream.Collectors;

import com.github.zerorooot.bean.FileBean;
import com.github.zerorooot.serve.FileServe;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.NoArgsConstructor;



/**
 * @Author: zero
 * @Date: 2020/8/10 19:30
 */

@NoArgsConstructor
public class PictureView extends Application {
    private FileBean fileBean;
    private String cookie;
    private int currentIndex = 0;

    public PictureView(FileBean fileBean, String cookie) {
        this.fileBean = fileBean;
        this.cookie = cookie;
    }

    public void start(Stage primaryStage) {
        ImageView imageView = new ImageView();
        FileServe fileServe = new FileServe(cookie);
        String file = fileServe.download(fileBean);

        List<FileBean> pictureArrayList =
                fileServe.getNonDirectory(fileBean.getParentPath()).stream().filter(e -> e.getMime().contains("image")).collect(Collectors.toList());

        for (int i = 0; i < pictureArrayList.size(); i++) {
            if (fileBean.getPath().equals(pictureArrayList.get(i).getPath())) {
                currentIndex = i;
                break;
            }
        }

        Image image = new Image(file);
        imageView.setImage(image);


        final double w = image.getWidth();
        final double h = image.getHeight();
        final double max = Math.max(w, h);
        final int width = (int) (500 * w / max);
        final int height = (int) (500 * h / max);
        imageView.setFitHeight(height);
        imageView.setFitWidth(width);
        imageView.setCache(true);

        Pane pane = new Pane();
        StackPane stackPane = new StackPane(pane);

        Scene scene = new Scene(stackPane, 700, 500);
        pane.getChildren().add(imageView);

        pane.heightProperty().addListener((observable, oldValue, newValue) -> {
            imageView.setY((newValue.doubleValue() - imageView.getFitHeight()) / 2);
        });

        pane.widthProperty().addListener((observable, oldValue, newValue) -> {
            imageView.setX((newValue.doubleValue() - imageView.getFitWidth()) / 2);
        });


        final double scale = 5;
        stackPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            double rate;
            if (event.getDeltaY() > 0) {
                rate = 0.05;
            } else {
                rate = -0.05;
            }
            double newWidth = imageView.getFitWidth() + w * rate;
            double newHeight = imageView.getFitHeight() + h * rate;
            if (newWidth <= width || newWidth > scale * width) {
                return;
            }
            Point2D eventPoint = new Point2D(event.getSceneX(), event.getSceneY());
            Point2D imagePoint = pane.localToScene(new Point2D(imageView.getX(), imageView.getY()));
            Rectangle2D imageRect = new Rectangle2D(imagePoint.getX(), imagePoint.getY(), imageView.getFitWidth(), imageView.getFitHeight());
            Point2D ratePoint;
            Point2D eventPointDistance;
            if (newWidth > scale / 4 * width && imageRect.contains(eventPoint)) {
                ratePoint = eventPoint.subtract(imagePoint);
                ratePoint = new Point2D(ratePoint.getX() / imageView.getFitWidth(), ratePoint.getY() / imageView.getFitHeight());
                eventPointDistance = pane.sceneToLocal(eventPoint);
            } else {
                ratePoint = new Point2D(0.5, 0.5);
                eventPointDistance = new Point2D(pane.getWidth() / 2,
                        pane.getHeight() / 2);
            }

            imageView.setX(eventPointDistance.getX() - newWidth * ratePoint.getX());
            imageView.setY(eventPointDistance.getY() - newHeight * ratePoint.getY());
            imageView.setFitWidth(newWidth);
            imageView.setFitHeight(newHeight);
        });


        scene.setOnKeyPressed(keyEvent -> {
            //上一张
            if (keyEvent.getCode() == KeyCode.LEFT && currentIndex > 0) {
                currentIndex = currentIndex - 1;
                FileBean currentFileBean = pictureArrayList.get(currentIndex);
                primaryStage.setTitle(currentFileBean.getName() + "  " + currentIndex + "/" + pictureArrayList.size());
                String file1 = fileServe.download(currentFileBean);

                Image image1 = new Image(file1);
                imageView.setImage(image1);
            }
            //下一张
            if (keyEvent.getCode() == KeyCode.RIGHT && currentIndex + 1 < pictureArrayList.size()) {
                currentIndex = currentIndex + 1;
                FileBean currentFileBean = pictureArrayList.get(currentIndex);
                primaryStage.setTitle(currentFileBean.getName() + "  " + currentIndex + "/" + pictureArrayList.size());
                String file1 = fileServe.download(currentFileBean);
                Image image1 = new Image(file1);
                imageView.setImage(image1);

            }
        });

        primaryStage.setTitle(fileBean.getName() + "  " + currentIndex + "/" + pictureArrayList.size());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
