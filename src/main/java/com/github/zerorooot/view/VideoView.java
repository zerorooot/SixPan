package com.github.zerorooot.view;

/**
 * @Author: zero
 * @Date: 2020/8/6 19:29
 */
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import static uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurfaceFactory.videoSurfaceForImageView;


public class VideoView extends Application {
    protected final MediaPlayerFactory mediaPlayerFactory;
    protected final EmbeddedMediaPlayer embeddedMediaPlayer;
    private String url;
    private String name;


    public VideoView(String url, String name) {
        this.mediaPlayerFactory = new MediaPlayerFactory();
        this.embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        this.url = url;
        this.name = name;

    }


    @Override
    public final void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1280, 760, Color.BLACK);
        ProgressBar progressBar = new ProgressBar(0);
        ImageView videoImageView = new ImageView();
        videoImageView.setPreserveRatio(true);
        videoImageView.setSmooth(true);
        embeddedMediaPlayer.videoSurface().set(videoSurfaceForImageView(videoImageView));

        root.setCenter(videoImageView);
        scene.widthProperty().addListener(e->{
            videoImageView.setFitWidth(scene.getWidth());
        });
        scene.heightProperty().addListener(e->{
            videoImageView.setFitHeight(scene.getWidth() - progressBar.getHeight());
        });
        primaryStage.setResizable(false);

       scene.setOnMouseClicked(e->{
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
        });
        progressBar.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        root.setBottom(progressBar);

        embeddedMediaPlayer.media().play(url);

        scene.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.RIGHT) {
                embeddedMediaPlayer.controls().skipTime(15000);
            }
            if (keyEvent.getCode() == KeyCode.LEFT) {
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
            progressBar.setProgress(embeddedMediaPlayer.status().position());

        });
        scene.setOnKeyReleased(keyEvent->{
            if (keyEvent.getCode() == KeyCode.SPACE) {
                embeddedMediaPlayer.controls().pause();
            }
        });

        primaryStage.setTitle(name);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
