package com.github.zerorooot.view;

import com.github.zerorooot.bean.FileBean;
import com.github.zerorooot.bean.ImageParameterBean;
import com.github.zerorooot.serve.FileServe;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.SneakyThrows;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * from https://gist.github.com/james-d/ce5ec1fd44ce6c64e81a
 */
public class PictureView extends Application {
    private static final int MIN_PIXELS = 10;
    private final ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();
    public ImageView imageView;
    private Image image;
    private ProgressIndicator progressIndicator;
    private Service<Integer> service;
    private final FileServe fileServe;

    private FileBean fileBean;
    private int currentIndex = 0;
    private List<FileBean> pictureArrayList;
    private ConcurrentHashMap<Integer, Image> imageCache;
    private ConcurrentHashMap<Integer, ImageParameterBean> imageParameterCache;

    public PictureView(FileBean fileBean, String token) {
        this.fileBean = fileBean;
        this.fileServe = new FileServe(token);
    }

    @Override
    public void start(Stage primaryStage) {
        progressIndicator = new ProgressIndicator();
        StackPane stackPane = new StackPane();
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        stackPane.getChildren().add(imageView);
        service = loading(stackPane, primaryStage);

        pictureArrayList = initPictureArrayListAndImageCacheAndImageParameterCache(fileBean);

        primaryStage.setWidth(1280);
        primaryStage.setHeight(720);

        imageView.setOnMousePressed(this::setOnMousePressed);
        imageView.setOnMouseDragged(this::setOnMouseDragged);
        imageView.setOnScroll(this::setOnScrolled);
        imageView.setOnMouseClicked(this::setOnMouseClicked);
        imageView.fitWidthProperty().bind(stackPane.widthProperty());
        imageView.fitHeightProperty().bind(stackPane.heightProperty());

        Scene scene = new Scene(stackPane);
        primaryStage.setScene(scene);

        primaryStage.setTitle(fileBean.getName() + "  " + (currentIndex + 1) + "/" + pictureArrayList.size());
        primaryStage.show();
        stackPane.setAlignment(Pos.CENTER);
        service.start();

        scene.setOnKeyReleased(keyEvent -> upOrDownPicture(keyEvent, primaryStage));

    }


    private List<FileBean> initPictureArrayListAndImageCacheAndImageParameterCache(FileBean fileBean) {
        List<FileBean> pictureArrayList =
                fileServe.getNonDirectory(fileBean.getParentPath()).stream().filter(e -> e.getMime().contains("image")).collect(Collectors.toList());

        imageCache = new ConcurrentHashMap<>(pictureArrayList.size());

        imageParameterCache = new ConcurrentHashMap<>(pictureArrayList.size());

        for (int i = 0; i < pictureArrayList.size(); i++) {
            if (fileBean.getPath().equals(pictureArrayList.get(i).getPath())) {
                currentIndex = i;
                break;
            }
        }
        return pictureArrayList;
    }

    /**
     * show loading animation
     *
     * @param stackPane scene
     * @return Service
     */
    private Service<Integer> loading(StackPane stackPane, Stage primaryStage) {
        Service<Integer> service = new Service<>() {
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
                        image = getCacheImage();
                        double width = image.getWidth();
                        double height = image.getHeight();
                        imageView.setImage(image);
                        imageView.setRotate(0);

                        resetImageView(imageView, width, height);

                        Platform.runLater(() -> {
                            stackPane.getChildren().removeAll(stackPane.getChildren());
                            stackPane.getChildren().add(imageView);
                            ImageParameterBean imageParameterBean = getImageParameterCache(primaryStage);
                            primaryStage.setHeight(imageParameterBean.getHeight());
                            primaryStage.setWidth(imageParameterBean.getWidth());

                            //居中
                            primaryStage.centerOnScreen();
                        });

                        return null;
                    }
                };
            }
        };
        return service;
    }

    /**
     * 从缓存中获取图片
     *
     * @return 缓存中的图片
     */
    private Image getCacheImage() {
        if (imageCache.get(currentIndex) == null) {
            Image image = new Image(fileServe.download(fileBean));
            imageCache.put(currentIndex, image);
            return image;
        }
        return imageCache.get(currentIndex);
    }

    /**
     * 从缓存中获取图片参数
     *
     * @return 缓存中的图片参数
     */
    private ImageParameterBean getImageParameterCache(Stage primaryStage) {
        if (imageParameterCache.get(currentIndex) == null) {
            ImageParameterBean imageParameterBean = fileServe.imagePreview(fileBean);
            //预览不存在
            if (imageParameterBean.getHeight() == 0 && imageParameterBean.getWidth() == 0) {
                imageParameterBean.setHeight((int) primaryStage.getHeight());
                imageParameterBean.setWidth((int) (primaryStage.getHeight() * image.getWidth() / image.getHeight()));
            }
            imageParameterCache.put(currentIndex, imageParameterBean);
            return imageParameterBean;
        }
        return imageParameterCache.get(currentIndex);
    }


    /**
     * reset to the top left:
     *
     * @param imageView image
     * @param width     image width
     * @param height    image height
     */
    private void resetImageView(ImageView imageView, double width, double height) {
        imageView.setViewport(new Rectangle2D(0, 0, width, height));
    }

    /**
     * shift the viewport of the imageView by the specified delta, clamping so the viewport does not move off the actual image:
     *
     * @param imageView image
     * @param delta     delta
     */
    private void shift(ImageView imageView, Point2D delta) {
        Rectangle2D viewport = imageView.getViewport();

        double width = imageView.getImage().getWidth();
        double height = imageView.getImage().getHeight();

        double maxX = width - viewport.getWidth();
        double maxY = height - viewport.getHeight();

        double minX = clamp(viewport.getMinX() - delta.getX(), 0, maxX);
        double minY = clamp(viewport.getMinY() - delta.getY(), 0, maxY);

        imageView.setViewport(new Rectangle2D(minX, minY, viewport.getWidth(), viewport.getHeight()));
    }

    private double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    /**
     * convert mouse coordinates in the imageView to coordinates in the actual image:
     *
     * @param imageView            image
     * @param imageViewCoordinates imageViewCoordinates
     * @return 2d
     */
    private Point2D imageViewToImage(ImageView imageView, Point2D imageViewCoordinates) {
        double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
        double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();

        Rectangle2D viewport = imageView.getViewport();
        return new Point2D(
                viewport.getMinX() + xProportion * viewport.getWidth(),
                viewport.getMinY() + yProportion * viewport.getHeight());
    }

    private void setOnMousePressed(MouseEvent e) {
        Point2D mousePress = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));
        mouseDown.set(mousePress);
    }

    private void setOnMouseDragged(MouseEvent e) {
        Point2D dragPoint = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));
        shift(imageView, dragPoint.subtract(mouseDown.get()));
        mouseDown.set(imageViewToImage(imageView, new Point2D(e.getX(), e.getY())));
    }

    private void setOnScrolled(ScrollEvent e) {
        double width = image.getWidth();
        double height = image.getHeight();

        double delta = e.getDeltaY() * (-1);
        Rectangle2D viewport = imageView.getViewport();

        double scale = clamp(Math.pow(1.01, delta),
                // don't scale so we're zoomed in to fewer than MIN_PIXELS in any direction:
                Math.min(MIN_PIXELS / viewport.getWidth(), MIN_PIXELS / viewport.getHeight()),

                // don't scale so that we're bigger than image dimensions:
                Math.max(width / viewport.getWidth(), height / viewport.getHeight())

        );

        Point2D mouse = imageViewToImage(imageView, new Point2D(e.getX(), e.getY()));

        double newWidth = viewport.getWidth() * scale;
        double newHeight = viewport.getHeight() * scale;

        // To keep the visual point under the mouse from moving, we need
        // (x - newViewportMinX) / (x - currentViewportMinX) = scale
        // where x is the mouse X coordinate in the image

        // solving this for newViewportMinX gives

        // newViewportMinX = x - (x - currentViewportMinX) * scale

        // we then clamp this value so the image never scrolls out
        // of the imageview:

        double newMinX = clamp(mouse.getX() - (mouse.getX() - viewport.getMinX()) * scale,
                0, width - newWidth);
        double newMinY = clamp(mouse.getY() - (mouse.getY() - viewport.getMinY()) * scale,
                0, height - newHeight);
        imageView.setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
    }

    private void setOnMouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            double width = image.getWidth();
            double height = image.getHeight();
            resetImageView(imageView, width, height);
        }
    }

    /**
     * 上一张或下一张
     *
     * @param keyEvent     keyEvent
     * @param primaryStage primaryStage
     */
    private void upOrDownPicture(KeyEvent keyEvent, Stage primaryStage) {
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
        //顺时针旋转图片
        if (keyEvent.getCode() == KeyCode.UP) {
            imageView.setRotate(imageView.getRotate() + 90);
        }

        //逆时针旋转图片
        if (keyEvent.getCode() == KeyCode.DOWN) {
            imageView.setRotate(imageView.getRotate() - 90);
        }
    }
}

