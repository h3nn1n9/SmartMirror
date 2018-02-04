package main;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;


public class FensterController {

    @FXML Label zeit;
    @FXML VBox NewsBox;
    @FXML ImageView wetter;
    @FXML Label temperatur;


    public void setTime(String time){
        System.out.println("Set Time");
        zeit.setText(time);
    }


    public void initializefenster() {
        zeit.setFont(new Font("Time new Roman", 70));
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
    }

    public VBox getNewsBox(){
        return NewsBox;
    }

    public Label getZeitLabel(){
        return zeit;
    }

    public  ImageView getWetterIcon(){
        return wetter;
    }

    public Label getTemperatur() {
        return temperatur;
    }
}
