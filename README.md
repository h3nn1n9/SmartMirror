# SmartMirror

A JavaFX desktop application that transforms a monitor — mounted behind a two-way mirror — into an ambient information display.

![SmartMirror preview](src/main/resources/res/sonne_wolken.png)

## Features

- **Clock** — live time display, updated every second
- **Weather** — current conditions and temperature from OpenWeatherMap (sun, rain, clouds, moon icons for day/night)
- **News** — top 6 headlines for your country from NewsAPI
- **Calendar** — upcoming Google Calendar events via OAuth2

---

## Requirements

| Tool | Version |
|---|---|
| JDK | 21 |
| Maven | 3.8+ |
| A graphical display | (JavaFX cannot run headlessly) |

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/h3nn1n9/SmartMirror.git
cd SmartMirror
```

### 2. Configure API keys

Copy the template and fill in your credentials:

```bash
cp src/main/resources/application.properties src/main/resources/application.properties.local
```

Edit `src/main/resources/application.properties` (or set environment variables — see below):

```properties
# OpenWeatherMap — https://openweathermap.org/api (free tier)
# Find your city ID at https://openweathermap.org/find
openweather.city.id=2820621
openweather.api.key=YOUR_KEY_HERE

# NewsAPI — https://newsapi.org (free tier)
news.api.key=YOUR_KEY_HERE
news.country=de          # ISO 3166-1 alpha-2 country code
```

**Alternatively**, set environment variables (Quarkus picks them up automatically):

```bash
export OPENWEATHER_CITY_ID=2820621
export OPENWEATHER_API_KEY=your_key
export NEWS_API_KEY=your_key
export NEWS_COUNTRY=de
```

### 3. Set up Google Calendar *(optional)*

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a project → Enable the **Google Calendar API**
3. Create OAuth 2.0 credentials → download as **`client_secret.json`**
4. Place `client_secret.json` in the project root directory

On first run a browser window opens for consent. Credentials are then cached at
`~/.credentials/calendar-java-quickstart`.

> **Skip this step** if you don't need calendar integration — the rest of the app works without it.

---

## Running the application

### Development mode (recommended)

Uses the `javafx-maven-plugin`, which handles the OpenJFX module path automatically:

```bash
mvn javafx:run
```

### Quarkus dev mode (hot reload)

```bash
mvn quarkus:dev
```

> Requires a connected display. Hot reload works for Java changes; FXML and resource changes require a restart.

### Build and run a packaged JAR

```bash
mvn package
```

The Quarkus build produces `target/quarkus-app/`. Run it with a local JavaFX SDK:

```bash
java --module-path /path/to/javafx-sdk/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar target/quarkus-app/quarkus-run.jar
```

> Download the JavaFX SDK for your platform at [gluonhq.com/products/javafx](https://gluonhq.com/products/javafx/).

---

## Project structure

```
SmartMirror/
├── src/main/
│   ├── java/main/
│   │   ├── GUI.java                # Quarkus entry point + JavaFX bootstrap
│   │   ├── FensterController.java  # FXML controller (@Inject services)
│   │   ├── WeatherService.java     # Weather business logic
│   │   ├── NewsService.java        # News business logic
│   │   ├── CalendarService.java    # Google Calendar integration
│   │   ├── WeatherClient.java      # REST client for OpenWeatherMap
│   │   └── NewsClient.java         # REST client for NewsAPI
│   └── resources/
│       ├── main/gui.fxml           # JavaFX layout
│       ├── res/                    # Weather icons (PNG)
│       └── application.properties  # All configuration
├── client_secret.json              # Google OAuth2 credentials (not committed)
└── pom.xml
```

---

## Configuration reference

All settings live in `src/main/resources/application.properties`.
Every key can be overridden by an environment variable — Quarkus converts `openweather.api.key` → `OPENWEATHER_API_KEY` automatically.

| Key | Default | Description |
|---|---|---|
| `openweather.city.id` | — | OpenWeatherMap city ID |
| `openweather.api.key` | — | OpenWeatherMap API key |
| `news.api.key` | — | NewsAPI key |
| `news.country` | `de` | Two-letter country code for headlines |
| `quarkus.http.host-enabled` | `false` | Keep HTTP server off (desktop app) |

---

## Architecture overview

Quarkus manages the CDI container and configuration. JavaFX provides the UI.

```
main()
  └── Quarkus.run()          ← starts CDI container + config
        └── GUI.run()
              └── Application.launch()   ← starts JavaFX thread
                    └── FXMLLoader (CDI controller factory)
                          └── FensterController
                                ├── @Inject WeatherService → WeatherClient (REST)
                                └── @Inject NewsService    → NewsClient    (REST)
```

The controller is `@Dependent` (not `@ApplicationScoped`) so CDI does not wrap it in a proxy — required for JavaFX `@FXML` field injection to work via reflection.

---

## Known limitations

- API calls run synchronously on the JavaFX Application Thread — the UI is briefly frozen on startup
- Layout uses absolute pixel positions — does not adapt to non-standard screen sizes
- No error handling around network calls — a failing API will crash the app silently

---

## License

This project does not currently have a license file.
