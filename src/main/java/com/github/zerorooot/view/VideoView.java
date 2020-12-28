package com.github.zerorooot.view;


import com.github.zerorooot.bean.FileBean;
import com.github.zerorooot.bean.OffLineBean;
import com.github.zerorooot.serve.FileServe;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.*;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import java.util.Optional;

import static uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurfaceFactory.videoSurfaceForImageView;


/**
 * @author ubuntu
 */
public class VideoView extends Application {
    protected final MediaPlayerFactory mediaPlayerFactory;
    protected final EmbeddedMediaPlayer embeddedMediaPlayer;
    private final String url;
    private final String name;

    private final FileServe fileServe;
    private OffLineBean offLineBean;
    private FileBean fileBean;

    private Stage primaryStage;
    private ImageView videoImageView;
    private ProgressBar progressBar;

    public VideoView(FileBean fileBean, FileServe fileServe) {
        this.mediaPlayerFactory = new MediaPlayerFactory();
        this.embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        this.fileBean = fileBean;
        this.fileServe = fileServe;

        this.url = fileServe.download(fileBean);
        this.name = fileBean.getName();
    }

    public VideoView(OffLineBean offLineBean, FileServe fileServe) {
        this.mediaPlayerFactory = new MediaPlayerFactory();
        this.embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        this.fileServe = fileServe;
        this.offLineBean = offLineBean;

        this.url = fileServe.download(offLineBean);
        this.name = offLineBean.getName();
    }

    @Override
    public final void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        double screenWidth = 1294;
        double screenHeight = 797;
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1280, 755, Color.BLACK);
        progressBar = new ProgressBar(0);
        videoImageView = new ImageView();
        videoImageView.setPreserveRatio(true);
        videoImageView.setSmooth(true);
        embeddedMediaPlayer.videoSurface().set(videoSurfaceForImageView(videoImageView));

        root.setCenter(videoImageView);
        root.setBottom(progressBar);

        scene.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                primaryStage.setFullScreen(true);
            }
        });
        progressBar.setOnMouseClicked(ev -> {
            double x = ev.getX();
            double progressPercent = x / progressBar.getWidth();
            embeddedMediaPlayer.controls().setPosition((float) progressPercent);
            progressBar.setProgress(progressPercent);
            primaryStage.setTitle(name + "    " + secondToTime(embeddedMediaPlayer.status().time() / 1000) + "/" + secondToTime(embeddedMediaPlayer.status().length() / 1000));
        });
        progressBar.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);


        embeddedMediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                progressBar.setProgress(embeddedMediaPlayer.status().position());
                Platform.runLater(() -> {
                    primaryStage.setTitle(name + "    " + secondToTime(embeddedMediaPlayer.status().time() / 1000) + "/" + secondToTime(embeddedMediaPlayer.status().length() / 1000));
                });
            }

            //403 error
            @Override
            public void error(MediaPlayer mediaPlayer) {
                RuntimeException ex = new RuntimeException("VLC 无法打开 MRL\n" + url);
                errorDialog("HTTP 403 error", ex);
            }
        });

        embeddedMediaPlayer.media().play(url);


        scene.setOnKeyPressed(this::borderPaneKeyPressed);

        scene.widthProperty().addListener(e -> {
            videoImageView.setFitWidth(scene.getWidth());
        });
        scene.heightProperty().addListener(e -> {
            videoImageView.setFitHeight(scene.getWidth() - progressBar.getHeight());
        });

        scene.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.SPACE) {
                embeddedMediaPlayer.controls().pause();
            }
        });

        //调整窗口大小
        embeddedMediaPlayer.media().events().addMediaEventListener(new MediaEventAdapter() {
            @Override
            public void mediaDurationChanged(Media media, long newDuration) {
                try {
                    double videoWidth = embeddedMediaPlayer.video().videoDimension().getWidth();
                    double videoHeight = embeddedMediaPlayer.video().videoDimension().getHeight();

                    videoImageView.setFitHeight(root.getCenter().getScene().getHeight() - progressBar.getHeight());
                    //竖屏
                    if (videoWidth <= videoHeight) {
                        primaryStage.setWidth(videoWidth);
                    }

                } catch (Exception e) {
                    errorDialog("open error", e);
                }


            }
        });

        primaryStage.setTitle(name);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);
    }

    /**
     * 秒转成正常时间
     *
     * @param mss 秒
     * @return 时分秒
     */
    private String secondToTime(long mss) {
        String hours = (mss % (60 * 60 * 24)) / (60 * 60) + "";
        String minutes = (mss % (60 * 60)) / 60 + "";
        String seconds = mss % 60 + "";
        if (hours.length() == 1) {
            hours = "0" + hours;
        }
        if (minutes.length() == 1) {
            minutes = "0" + minutes;
        }
        if (seconds.length() == 1) {
            seconds = "0" + seconds;
        }
        return hours + ":" + minutes + ":" + seconds;

    }

    /**
     * 快进，旋转 视频
     *
     * @param keyEvent 键盘按钮
     */
    public void borderPaneKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.RIGHT) {
            primaryStage.setTitle(name + "    " + secondToTime(embeddedMediaPlayer.status().time() / 1000) + "/" + secondToTime(embeddedMediaPlayer.status().length() / 1000));
            embeddedMediaPlayer.controls().skipTime(15000);
        }
        if (keyEvent.getCode() == KeyCode.LEFT) {
            primaryStage.setTitle(name + "    " + secondToTime(embeddedMediaPlayer.status().time() / 1000) + "/" + secondToTime(embeddedMediaPlayer.status().length() / 1000));
            embeddedMediaPlayer.controls().skipTime(-15000);
        }
        if (keyEvent.getCode() == KeyCode.UP) {
            embeddedMediaPlayer.audio().setVolume(embeddedMediaPlayer.audio().volume() + 1);
        }
        if (keyEvent.getCode() == KeyCode.DOWN) {
            embeddedMediaPlayer.audio().setVolume(embeddedMediaPlayer.audio().volume() - 1);
        }
        if (keyEvent.getCode() == KeyCode.ESCAPE) {
            primaryStage.setFullScreen(false);
        }
        //顺时针旋转视频
        if (keyEvent.getCode() == KeyCode.A) {
            videoImageView.setRotate(videoImageView.getRotate() + 90);
        }
        //逆时针旋转视频
        if (keyEvent.getCode() == KeyCode.D) {
            videoImageView.setRotate(videoImageView.getRotate() - 90);
        }
        progressBar.setProgress(embeddedMediaPlayer.status().position());
    }

    /**
     * show error dialog and delete video
     *
     * @param errorMessage errorMessage
     * @param e            {@link Exception}
     */
    public void errorDialog(String errorMessage, Exception e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR Dialog");
            alert.setHeaderText(errorMessage);
            alert.setContentText(e.getMessage());
// Create expandable Exception.
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label("The exception stacktrace was:");

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            // Set expandable Exception into the dialog pane.
            alert.getDialogPane().setExpandableContent(expContent);
            ButtonType buttonTypeOk = new ButtonType("OK");
            ButtonType buttonTypeDeleteVideo = new ButtonType("删除视频");
            alert.getButtonTypes().setAll(buttonTypeOk, buttonTypeDeleteVideo);

            Optional<ButtonType> result = alert.showAndWait();
            result.ifPresent(buttonType -> {
                if (buttonType == buttonTypeDeleteVideo) {
                    if (Objects.nonNull(fileBean)) {
                        fileServe.delete(fileBean);
                    }
                    if (Objects.nonNull(offLineBean)) {
                        fileServe.offLineDelete(offLineBean);
                    }

                }
                primaryStage.close();
            });

        });

    }

    /**
     * release vlcj
     *
     * @param event close window
     */
    private void closeWindowEvent(WindowEvent event) {
        mediaPlayerFactory.release();
    }
}
