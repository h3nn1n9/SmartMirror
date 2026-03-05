package main;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.enterprise.inject.spi.CDI;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Application entry point.
 *
 * Quarkus starts first (CDI container, config, REST clients), then launches
 * JavaFX. The FXMLLoader uses CDI to instantiate the controller so that
 * @Inject fields in FensterController are resolved before @FXML fields and
 * initialize() are processed.
 *
 * Thread model:
 *   main thread  → Quarkus.run() → GUI.run() → Application.launch() (blocks)
 *   JavaFX thread → SmartMirrorApp.start()
 */
@QuarkusMain
public class GUI implements QuarkusApplication {

    public static void main(String[] args) {
        Quarkus.run(GUI.class, args);
    }

    @Override
    public int run(String... args) throws Exception {
        Application.launch(SmartMirrorApp.class, args);
        return 0;
    }

    public static class SmartMirrorApp extends Application {

        @Override
        public void start(Stage primaryStage) throws Exception {
            var loader = new FXMLLoader(getClass().getResource("/main/gui.fxml"));

            // Let CDI instantiate the controller so @Inject fields are populated
            // before FXMLLoader injects @FXML fields and calls initialize().
            loader.setControllerFactory(clazz -> CDI.current().select(clazz).get());

            primaryStage.setScene(new Scene(loader.load()));
            primaryStage.setTitle("SmartMirror");
            primaryStage.setFullScreen(true);
            primaryStage.show();
        }
    }
}
