package main;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;


public class GUI extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        var loader = new FXMLLoader(getClass().getResource("gui.fxml"));
        var newScene = new Scene(loader.load());

        FensterController controller = loader.getController();
        controller.initializefenster();
        VBox newsBox = controller.getNewsBox();
        ImageView wetterIcon = controller.getWetterIcon();
        Label temperatur = controller.getTemperatur();
        setNews(newsBox);
        setWetterData(wetterIcon, temperatur);

        primaryStage.setTitle("GUI");
        primaryStage.setScene(newScene);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void setWetterData(ImageView wetterIcon, Label temperatur) throws Exception {
        var openweather = new URL("http://api.openweathermap.org/data/2.5/weather?id=2820621&APPID=72e171e11967724100a2bc62b8156741");
        var yc = openweather.openConnection();
        String inputLine;
        try (var in = new BufferedReader(new InputStreamReader(yc.getInputStream()))) {
            inputLine = in.readLine();
        }

        var jo = new JSONObject(inputLine);
        var aktweather = jo.getJSONArray("weather").getJSONObject(0);

        long sunset = jo.getJSONObject("sys").getLong("sunset");
        var sunsetTime = Instant.ofEpochSecond(sunset).atZone(ZoneId.systemDefault()).toLocalTime();

        switch (aktweather.getString("main")) {
            case "Rain"   -> wetterIcon.setImage(new Image("/res/regen.png"));
            case "Clouds" -> wetterIcon.setImage(new Image("/res/wolken.png"));
            case "Clear"  -> {
                var icon = LocalTime.now().isAfter(sunsetTime) ? "/res/mond.png" : "/res/sonne.png";
                wetterIcon.setImage(new Image(icon));
            }
            default -> System.out.println("Unknown weather condition: " + aktweather.getString("main"));
        }

        double temp = ((int) ((jo.getJSONObject("main").getInt("temp") - 273.15) * 10)) / 10.0;
        temperatur.setText(temp + "°C");
        temperatur.setFont(new Font("Arial", 90));
    }

    public static void setNews(VBox newsBox) throws Exception {
        var headlines = new URL("https://newsapi.org/v2/top-headlines?country=de&apiKey=e6f838bd92694c3cbe8aa71e6cb4e6a9");
        var yc = headlines.openConnection();
        String inputLine;
        try (var in = new BufferedReader(new InputStreamReader(yc.getInputStream()))) {
            inputLine = in.readLine();
        }

        var alleArtikel = new JSONObject(inputLine);
        int totalResults = Math.min(alleArtikel.getInt("totalResults"), 6);
        for (int i = 0; i < totalResults; i++) {
            var artikel = alleArtikel.getJSONArray("articles").getJSONObject(i);
            var label = new Label(artikel.getString("title") + "  -  " + artikel.getJSONObject("source").getString("name"));
            newsBox.getChildren().add(label);
        }
    }


    /** Application name. */
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/calendar-java-quickstart");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/calendar-java-quickstart
     */
    private static final List<String> SCOPES =
            Collections.singletonList(CalendarScopes.CALENDAR_READONLY);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        InputStream in = new FileInputStream("client_secret.json");
        var clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        var flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     * @return an authorized Calendar client service
     * @throws IOException
     */
    public static com.google.api.services.calendar.Calendar getCalendarService() throws IOException {
        var credential = authorize();
        return new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void getCalendar() throws IOException {
        var service = getCalendarService();

        var now = new DateTime(System.currentTimeMillis());
        Events events = service.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        List<Event> items = events.getItems();
        if (items.isEmpty()) {
            System.out.println("No upcoming events found.");
        } else {
            System.out.println("Upcoming events");
            for (var event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    start = event.getStart().getDate();
                }
                System.out.printf("%s (%s)%n", event.getSummary(), start);
            }
        }
    }
}
