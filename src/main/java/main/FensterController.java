package main;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * FXML controller for the SmartMirror main view.
 *
 * Scope is @Dependent (not @ApplicationScoped) so CDI returns the actual
 * instance without a proxy — required for FXMLLoader to inject @FXML fields
 * directly via reflection.
 *
 * Lifecycle when the FXML is loaded:
 *   1. CDI creates this bean and resolves all @Inject fields.
 *   2. FXMLLoader injects @FXML fields.
 *   3. FXMLLoader calls initialize() — both sets of fields are ready.
 */
@Dependent
public class FensterController {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);

    @Inject
    WeatherService weatherService;

    @Inject
    NewsService newsService;

    @FXML private Label zeit;
    @FXML private VBox NewsBox;
    @FXML private ImageView wetter;
    @FXML private Label temperatur;

    @FXML
    void initialize() {
        startClock();
        loadWeather();
        loadNews();
    }

    private void startClock() {
        zeit.setFont(new Font("Times New Roman", 70));
        var timeline = new Timeline(new KeyFrame(
                Duration.seconds(1),
                e -> zeit.setText(LocalTime.now().format(TIME_FORMATTER))
        ));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void loadWeather() {
        var data = weatherService.load();
        String iconPath = switch (data.condition()) {
            case "Rain"   -> "/res/regen.png";
            case "Clouds" -> "/res/wolken.png";
            case "Clear"  -> LocalTime.now().isAfter(data.sunsetTime())
                             ? "/res/mond.png" : "/res/sonne.png";
            default       -> "/res/sonne_wolken.png";
        };
        wetter.setImage(new Image(iconPath));
        temperatur.setText(data.tempCelsius() + "°C");
        temperatur.setFont(new Font("Arial", 90));
    }

    private void loadNews() {
        newsService.getHeadlines()
                .stream()
                .map(Label::new)
                .forEach(NewsBox.getChildren()::add);
    }
}
