package main;

import com.sun.javafx.tk.Toolkit;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Observable;


public class FensterController {

    @FXML Label zeit;


    public void setTime(String time){
        System.out.println("Set Time");
        zeit.setText(time);
    }


    public void initializefenster() {
        long endTime = 100000000;
        zeit.setFont(new Font("Time new Roman", 70));
        DateFormat timeFormat = new SimpleDateFormat( "HH:mm:ss" );
        final Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        event -> {
                            zeit.setText(LocalTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)));
                        }
                )
        );
        timeline.setCycleCount( Animation.INDEFINITE );
        timeline.play();
        /*
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            LocalDateTime time = LocalDateTime.now();
                            DateTimeFormatter format = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);
                            String text = time.format(format);
                            setTime(text);
                            System.out.println("Text: " + zeit.getText());
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
        t.start();  */
    }


    public Label getZeitLabel(){
        return zeit;
    }
}
