package com.github.zerorooot.view;

/**
 * @Author: zero
 * @Date: 2020/8/6 19:29
 */

import cn.hutool.core.date.DateUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.*;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;


import java.util.Date;

import static uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurfaceFactory.videoSurfaceForImageView;


public class VideoView extends Application {
    protected final MediaPlayerFactory mediaPlayerFactory;
    protected final EmbeddedMediaPlayer embeddedMediaPlayer;
    private final String url;
    private final String name;


    public VideoView(String url, String name) {
        this.mediaPlayerFactory = new MediaPlayerFactory();
        this.embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        this.url = url;
        this.name = name;

    }


    @Override
    public final void start(Stage primaryStage) {
        double screenWidth = 1294;
        double screenHeight = 797;
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1280, 755, Color.BLACK);
        ProgressBar progressBar = new ProgressBar(0);
        ImageView videoImageView = new ImageView();
        videoImageView.setPreserveRatio(true);
        videoImageView.setSmooth(true);
        embeddedMediaPlayer.videoSurface().set(videoSurfaceForImageView(videoImageView));

        root.setCenter(videoImageView);


        scene.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                primaryStage.setFullScreen(true);
            }
        });

        progressBar.setOnMouseClicked(ev ->
        {
            double x = ev.getX();
            double progressPercent = x / progressBar.getWidth();
            embeddedMediaPlayer.controls().setPosition((float) progressPercent);
            progressBar.setProgress(progressPercent);
            primaryStage.setTitle(name + "    " + secondToTime(embeddedMediaPlayer.status().time() / 1000) + "/" + secondToTime(embeddedMediaPlayer.status().length() / 1000));
        });
        progressBar.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        root.setBottom(progressBar);

        embeddedMediaPlayer.media().play(url);

        scene.setOnKeyPressed(keyEvent -> {
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
        });

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
                double videoWidth = embeddedMediaPlayer.video().videoDimension().getWidth();
                double videoHeight = embeddedMediaPlayer.video().videoDimension().getHeight();
                if ((videoHeight < screenHeight & videoWidth > screenWidth) || (videoHeight > screenHeight & videoWidth < screenWidth)) {
                    primaryStage.setWidth(Double.min(videoWidth, screenWidth));
                    primaryStage.setHeight(Double.min(videoHeight, screenHeight));
                }
            }
        });

        primaryStage.setTitle(name);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    /**
     * 秒转成正常时间
     *
     * @param mss
     * @return
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
}
