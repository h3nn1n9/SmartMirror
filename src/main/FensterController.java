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

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);

    @FXML Label zeit;
    @FXML VBox NewsBox;
    @FXML ImageView wetter;
    @FXML Label temperatur;

    public void initializefenster() {
        zeit.setFont(new Font("Times New Roman", 70));
        final Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        event -> zeit.setText(LocalTime.now().format(TIME_FORMATTER))
                )
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public VBox getNewsBox() {
        return NewsBox;
    }

    public ImageView getWetterIcon() {
        return wetter;
    }

    public Label getTemperatur() {
        return temperatur;
    }
}
