# CLAUDE.md — SmartMirror Codebase Guide

This file provides context for AI assistants working in this repository.

---

## Project Overview

**SmartMirror** is a JavaFX desktop application that turns a monitor (typically mounted behind a two-way mirror) into an informational display. It shows:

- Current time (updated every second)
- Live weather data and icon (OpenWeatherMap API)
- Top news headlines (NewsAPI)
- Upcoming Google Calendar events (OAuth2)

**Language**: Java 21
**UI Framework**: OpenJFX 21
**Application Framework**: Quarkus 3.8.4 (CDI, config, REST client)
**Build System**: Maven (`pom.xml`)
**Status**: Early-stage / prototype (no tests)

---

## Repository Structure

```
SmartMirror/
├── src/
│   ├── main/
│   │   ├── java/main/
│   │   │   ├── GUI.java              # @QuarkusMain entry point + JavaFX Application
│   │   │   ├── FensterController.java# @Dependent FXML controller, wires services via @Inject
│   │   │   ├── WeatherService.java   # @ApplicationScoped, calls OpenWeatherMap
│   │   │   ├── NewsService.java      # @ApplicationScoped, calls NewsAPI
│   │   │   ├── CalendarService.java  # @ApplicationScoped, Google Calendar OAuth2
│   │   │   ├── WeatherClient.java    # @RegisterRestClient interface + response records
│   │   │   └── NewsClient.java       # @RegisterRestClient interface + response records
│   │   └── resources/
│   │       ├── main/
│   │       │   └── gui.fxml          # JavaFX layout (AnchorPane 600×400)
│   │       ├── res/                  # Weather icon assets (PNG)
│   │       │   ├── sonne.png         # Clear sky / day
│   │       │   ├── mond.png          # Clear sky / night
│   │       │   ├── regen.png         # Rain
│   │       │   ├── wolken.png        # Cloudy
│   │       │   ├── sonne_wolken.png  # Partly cloudy (default)
│   │       │   └── wolken_nacht.png  # Night cloudy
│   │       └── application.properties# All runtime config (API keys, URLs, Quarkus)
└── client_secret.json                # Google OAuth2 credentials (DO NOT COMMIT)
```

---

## Architecture

Quarkus acts as the IoC container and bootstrap framework. JavaFX provides the UI.

```
Quarkus (CDI container)
  └── @ApplicationScoped WeatherService  ──► WeatherClient (@RestClient)
  └── @ApplicationScoped NewsService     ──► NewsClient    (@RestClient)
  └── @ApplicationScoped CalendarService ──► Google Calendar SDK
  └── @Dependent FensterController       ──► @Inject WeatherService, NewsService
                                             @FXML Label, VBox, ImageView …
                                             initialize() → startClock() + loadWeather() + loadNews()

JavaFX (Application thread)
  └── GUI (extends Application)
        └── FXMLLoader  ──controllerFactory──► CDI.current().select(FensterController)
        └── Stage / Scene
```

### Controller lifecycle
1. `GUI.run()` calls `Application.launch()` — Quarkus CDI is already running.
2. `FXMLLoader` calls `controllerFactory` for `FensterController`.
3. CDI creates the `@Dependent` bean and resolves all `@Inject` fields.
4. `FXMLLoader` injects `@FXML` fields via reflection into the real instance (no proxy).
5. `FXMLLoader` calls `initialize()` — all fields are populated.

---

## Key Source Files

### `GUI.java` — Entry Point
- Implements `QuarkusApplication` and contains inner `SmartMirrorApp extends Application`
- `main()` → `Quarkus.run(GUI.class)` starts CDI, then calls `run()`
- `run()` → `Application.launch(SmartMirrorApp.class)` blocks until JavaFX exits
- `SmartMirrorApp.start()` loads `gui.fxml` with a CDI-aware controller factory

### `FensterController.java` — FXML Controller
- `@Dependent` scope — no CDI proxy, required for `@FXML` field injection to work
- `@Inject WeatherService` / `@Inject NewsService`
- `@FXML void initialize()` — called automatically by `FXMLLoader` after all fields are set
- Self-contained: `initialize()` drives all setup; no public API needed

### `WeatherClient.java` / `NewsClient.java` — REST Clients
- MicroProfile `@RegisterRestClient` interfaces
- API responses modelled as nested Java records (Jackson deserialises automatically)
- Base URLs configured in `application.properties`

### `WeatherService.java` / `NewsService.java` — Services
- `@ApplicationScoped` — one instance for the app lifetime
- API key and country injected via `@ConfigProperty`
- Return typed value objects (`WeatherData` record, `List<String>`)

### `CalendarService.java` — Google Calendar
- `@ApplicationScoped` bean wrapping the Google Calendar Java SDK
- OAuth2 flow unchanged — reads `client_secret.json` from the working directory

### `gui.fxml` — Layout
- Root: `AnchorPane` 600×400 px, black background, absolute pixel positioning
- Controller set to `main.FensterController`
- Default weather image: `@../res/sonne_wolken.png` (classpath-relative from `main/`)

---

## Technology Stack

| Component | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| UI | OpenJFX | 21.0.2 |
| App framework | Quarkus | 3.8.4 |
| CDI | Quarkus Arc | (bundled) |
| REST client | quarkus-rest-client-reactive-jackson | (bundled) |
| Config | MicroProfile Config | (bundled) |
| JSON | Jackson (via Quarkus) | (bundled) |
| Google Calendar | Google API Client | v3, rev20220715-1.32.1 |
| Google Auth | Google OAuth2 Client | 1.34.1 |
| Weather | OpenWeatherMap REST | v2.5 |
| News | NewsAPI REST | v2 |

---

## Build & Run

### Prerequisites
- JDK 21
- Maven 3.x (`mvn --version` to verify)
- `client_secret.json` in the project root (Google OAuth2)

### Common Maven commands

| Command | Effect |
|---|---|
| `mvn compile` | Compile all sources |
| `mvn quarkus:dev` | Dev mode with hot reload (requires a display) |
| `mvn package` | Build `target/quarkus-app/` |
| `mvn javafx:run` | Run via javafx-maven-plugin |
| `mvn clean` | Delete `target/` |

> **Note**: Use `mvn javafx:run` or `mvn quarkus:dev` — both handle the OpenJFX module
> path automatically.

### Run the packaged app

```bash
java --module-path /path/to/javafx-sdk/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar target/quarkus-app/quarkus-run.jar
```

> Quarkus produces `target/quarkus-app/quarkus-run.jar` (not a single fat JAR).
> JavaFX native libs must be on the module path at runtime.

---

## Configuration (`application.properties`)

All runtime configuration lives in `src/main/resources/application.properties`.
No values are hardcoded in Java source.

| Key | Purpose |
|---|---|
| `openweather.city.id` | OpenWeatherMap city ID |
| `openweather.api.key` | OpenWeatherMap API key |
| `quarkus.rest-client.openweather.url` | Base URL for `WeatherClient` |
| `news.api.key` | NewsAPI key |
| `news.country` | Two-letter country code for headlines |
| `quarkus.rest-client.newsapi.url` | Base URL for `NewsClient` |
| `quarkus.http.host-enabled` | `false` — no HTTP server for a desktop app |

Quarkus also supports environment variable overrides automatically:
`OPENWEATHER_API_KEY=…` overrides `openweather.api.key` at runtime.

---

## Naming Conventions

The codebase mixes **German and English** identifiers (original author is German):

| German term | English meaning |
|---|---|
| `Fenster` | Window |
| `Wetter` | Weather |
| `zeit` | time |
| `Temperatur` | Temperature |
| `NewsBox` | News container |
| `Mond` | Moon |
| `Sonne` | Sun |
| `Wolken` | Clouds |
| `Regen` | Rain |

When adding new code, prefer **English**. Do not rename existing `@FXML`-bound identifiers — `fx:id` in `gui.fxml` must match the field names exactly.

**Method naming**: camelCase · **Class naming**: PascalCase · **FXML IDs**: camelCase

---

## Security Issues (Critical)

1. **API keys in `application.properties`** — move to environment variables or a secrets manager; add the file to `.gitignore` in production
2. **`client_secret.json`** in the working directory — never commit; add to `.gitignore`

Add to `.gitignore`:
```
client_secret.json
.env
StoredCredential
```

---

## Known Technical Debt

- `FensterController.initialize()` makes synchronous HTTP calls on the JavaFX Application Thread → UI freezes during startup. Fix: use `Task<Void>` or `CompletableFuture` to load data off-thread, then update UI with `Platform.runLater()`
- `gui.fxml` uses absolute pixel positions — layout breaks at non-standard resolutions
- No error handling around REST calls — any API failure crashes the app silently
- `CalendarService` still uses the legacy Google Java SDK instead of a Quarkus REST client

---

## Testing

There are **no tests** in this project.

If adding tests:
- Add `quarkus-junit5` to `pom.xml`
- Use `@QuarkusTest` for integration tests
- Mock REST clients with `@InjectMock` + `@RestClient`
- Test services independently from UI

---

## Git Conventions

- **Main branch**: `master`
- **Commit messages**: imperative English ("Add error handling for weather API")
- **No pre-commit hooks** active

---

## What AI Assistants Should Know

1. **Standard Maven layout** — sources in `src/main/java/`, resources in `src/main/resources/`
2. **Quarkus is the framework** — use CDI (`@Inject`, `@ApplicationScoped`, `@Dependent`), `@ConfigProperty`, `@RegisterRestClient`
3. **Use `mvn quarkus:dev`** for development; `mvn javafx:run` also works
4. **`FensterController` must stay `@Dependent`** — other CDI scopes create proxies that break `@FXML` field injection
5. **Do not rename `@FXML`-bound fields** — `fx:id` in `gui.fxml` must match exactly
6. **Do not commit** `client_secret.json` or API keys
7. **The app requires a display** — JavaFX cannot run headlessly without extra config
8. **Java 21 features are available** — records, switch expressions, `var`, sealed classes
9. **JPMS is not used** — no `module-info.java`
10. **`application.properties` is the single config source** — inject new settings with `@ConfigProperty`
