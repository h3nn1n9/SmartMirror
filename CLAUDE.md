# CLAUDE.md — SmartMirror Codebase Guide

This file provides context for AI assistants working in this repository.

---

## Project Overview

**SmartMirror** is a JavaFX desktop application that turns a monitor (typically mounted behind a two-way mirror) into an informational display. It shows:

- Current time (updated every second)
- Live weather data and icon (OpenWeatherMap API)
- Top news headlines (NewsAPI)
- Upcoming Google Calendar events (OAuth2)

**Language**: Java 8
**UI Framework**: JavaFX (bundled with JDK 8)
**Build System**: Maven (`pom.xml`)
**IDE**: IntelliJ IDEA (project files present)
**Status**: Early-stage / prototype (3 commits, no tests)

---

## Repository Structure

```
SmartMirror/
├── src/
│   ├── main/
│   │   ├── GUI.java              # Application entry point & API integration
│   │   ├── FensterController.java# FXML controller, manages UI components
│   │   ├── Uhr.java              # Legacy clock thread (superseded, dead code)
│   │   └── gui.fxml              # JavaFX layout (AnchorPane, 600×400)
│   └── res/                      # Weather icon assets (PNG)
│       ├── sonne.png             # Clear sky
│       ├── regen.png             # Rain
│       ├── wolken.png            # Cloudy
│       ├── sonne_wolken.png      # Partly cloudy (default)
│       ├── mond.png              # Night clear
│       └── wolken_nacht.png      # Night cloudy
├── google-api-services-calendar-v3-rev287-java-1.23.0/  # Google Calendar library
├── jackson-all-1.9.0.jar         # JSON library (legacy Jackson)
├── java-json.jar                 # org.json library
├── client_secret.json            # Google OAuth2 credentials (DO NOT COMMIT)
└── SmartMirror.iml               # IntelliJ module definition
```

---

## Key Source Files

### `GUI.java` — Main Application
- Extends `javafx.application.Application`
- `start()`: Loads `gui.fxml`, sets fullscreen, wires the controller, then calls `setWetterData()`, `setNews()`, `getCalendar()`
- `setWetterData()`: HTTP GET to OpenWeatherMap → parses JSON → sets weather icon and temperature on `FensterController`
- `setNews()`: HTTP GET to NewsAPI → parses JSON → creates `Label` nodes and injects into `NewsBox` VBox
- `getCalendar()` / `authorize()` / `getCalendarService()`: Google Calendar OAuth2 flow and event listing
- **API keys are hardcoded in this file** — see Security section below

### `FensterController.java` — FXML Controller
- Annotated with `@FXML` for all UI bindings
- `initializefenster()`: Starts a JavaFX `Timeline` that fires every second to call `setTime()`
- Exposes getters (`getNewsBox()`, `getWetterIcon()`, `getTemperatur()`) used by `GUI.java`
- UI components: `zeit` (time Label), `NewsBox` (VBox), `wetter` (ImageView), `temperatur` (Label)

### `Uhr.java` — Legacy Clock (Dead Code)
- Implements `Runnable`, runs an infinite loop updating time every second
- Superseded by the `Timeline` in `FensterController`; not currently instantiated
- Can be removed without impact

### `gui.fxml` — Layout
- Root: `AnchorPane` 600×400 px, black background
- Components use absolute pixel positioning — not responsive
- Controller class set to `main.FensterController`

---

## Technology Stack

| Component | Technology | Version |
|---|---|---|
| Language | Java | 8 (JDK 1.8) |
| UI | JavaFX | 8 (bundled with JDK) |
| JSON | org.json + Jackson | java-json.jar / 1.9.0 |
| Google Calendar | Google API Client | v3, rev287 |
| Google Auth | Google OAuth2 | 1.23.0 |
| Weather | OpenWeatherMap REST | v2.5 |
| News | NewsAPI REST | v2 |

---

## Build & Run

The project uses **Maven** as its build system (`pom.xml` in the project root).

### Prerequisites
- JDK 8 (JavaFX is bundled — no separate install needed)
- Maven 3.x (`mvn --version` to verify)
- `client_secret.json` in the project root (Google OAuth2)

### Common Maven commands

| Command | Effect |
|---|---|
| `mvn compile` | Compile all sources |
| `mvn package` | Compile + create fat JAR in `target/` |
| `mvn exec:java` | Run the app directly (requires a display) |
| `mvn clean` | Delete the `target/` directory |
| `mvn clean package` | Full rebuild |

### Run the packaged JAR
```bash
java -jar target/smartmirror-1.0-SNAPSHOT.jar
```

### Source / resource layout (non-standard)
Maven is configured to match the existing directory layout:

| Path on disk | Lands on classpath as | Reason |
|---|---|---|
| `src/main/GUI.java` | `main/GUI.class` | `<sourceDirectory>src</sourceDirectory>` |
| `src/main/gui.fxml` | `main/gui.fxml` | matches `getClass().getResource("gui.fxml")` |
| `src/res/*.png` | `res/*.png` | matches `new Image("/res/<name>.png")` |

### IntelliJ IDEA (legacy)
The `SmartMirror.iml` file is still present. IntelliJ can import the project via **File → Open** (select `pom.xml`) to use the Maven configuration instead.

On first run, a browser window opens for Google OAuth2 consent. Credentials are cached in `~/.credentials/calendar-java-quickstart`.

---

## External API Configuration

All API credentials are currently **hardcoded in `GUI.java`**. Locate and update them there:

| API | Where to get a key |
|---|---|
| OpenWeatherMap | https://openweathermap.org/api |
| NewsAPI | https://newsapi.org |
| Google Calendar | Google Cloud Console → OAuth2 credentials → download as `client_secret.json` |

`client_secret.json` must be present in the project root for Google Calendar auth to work.

---

## Naming Conventions

The codebase mixes **German and English** identifiers (original author is German):

| German term | English meaning |
|---|---|
| `Fenster` | Window |
| `Wetter` | Weather |
| `Uhr` / `zeit` | Clock / time |
| `Temperatur` | Temperature |
| `Nachrichten` / `News` | News |
| `Mond` | Moon |
| `Sonne` | Sun |
| `Wolken` | Clouds |
| `Regen` | Rain |

When adding new code, prefer **English** to gradually align the codebase, but do not rename existing identifiers in isolation as it will break FXML bindings.

**Method naming**: camelCase
**Class naming**: PascalCase
**FXML IDs**: camelCase (must match `@FXML` field names exactly)

---

## Security Issues (Critical)

1. **Hardcoded API keys** in `GUI.java` — move to a `.env` file or `config.properties` and add to `.gitignore`
2. **`client_secret.json`** contains OAuth2 credentials and is committed to the repo — add to `.gitignore` immediately
3. **No HTTPS certificate validation** on API HTTP calls — add proper SSL verification for production use

When fixing, add these to `.gitignore`:
```
client_secret.json
*.properties
.env
StoredCredential
```

---

## Known Technical Debt

- `Uhr.java` is dead code — can be safely deleted
- `gui.fxml` uses hardcoded pixel positions — layout breaks at non-standard resolutions
- No error handling around any HTTP or JSON parsing calls — any API failure crashes the app silently
- `jackson-all-1.9.0.jar` is a very old Jackson version (2012); consider upgrading or switching to `org.json` exclusively
- No null checks on API responses

---

## Testing

There are **no tests** in this project. No test framework (JUnit, etc.) is configured.

If adding tests:
- Create a `src/test/` directory
- Add JUnit 4 or 5 to the classpath
- Mock HTTP calls to external APIs (e.g., using WireMock or Mockito)
- Test JSON parsing logic independently from network calls

---

## Git Conventions

- **Main branch**: `master`
- **Commit messages**: No formal convention enforced; use imperative English (e.g., "Add error handling for weather API")
- **No pre-commit hooks** are active

---

## What AI Assistants Should Know

1. **Maven is the build system** — use `pom.xml`; do not add Gradle or Ant
2. **Do not rename German identifiers** that appear in `gui.fxml` — they are tightly coupled to `@FXML` annotations
3. **Do not commit** `client_secret.json` or any file containing API keys
4. **The app requires a display** (JavaFX needs a graphics context) — it cannot run headlessly without additional configuration
5. **`Uhr.java`** can be ignored or deleted; it has no active callers
6. **Java 8 features only** — no modules (JPMS), no records, no sealed classes
7. When modifying `gui.fxml`, verify `@FXML` field names in `FensterController.java` still match
