# GemsFX Copilot Instructions

## Project Overview

GemsFX is a JavaFX custom controls library. The `gemsfx` module targets Java 11 and JavaFX 17, while `gemsfx-demo` uses
Java 24 and JavaFX 25.0.2. It is a two-module Maven project:

- `gemsfx/` — the library (published to Maven Central as `com.dlsc.gemsfx:gemsfx`)
- `gemsfx-demo/` — standalone demo applications, one per control

## Build & Test Commands

```bash
# Build the entire project
./mvnw -B verify

# Run tests only (JUnit 4, no JavaFX toolkit needed for most tests)
./mvnw test -pl gemsfx

# Run a single test class
./mvnw test -pl gemsfx -Dtest=SessionManagerTest

# Run a specific demo app
mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.FilterViewApp
```

CI runs: `./mvnw -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar`1

## Architecture

### Control–Skin–CSS Pattern

Every control follows the standard JavaFX MVC split:

1. **Control class** (`com.dlsc.gemsfx.XxxControl`) — extends `Control` or `Region`
2. **Skin class** (`com.dlsc.gemsfx.skins.XxxControlSkin`) — extends `SkinBase<XxxControl>`
3. **CSS file** (`gemsfx/src/main/resources/com/dlsc/gemsfx/xxx-control.css`)

All three pieces are wired together in the control itself:

```java
// In the control constructor
getStyleClass().

add("xxx-control");  // kebab-case

// In the control class body
@Override
public String getUserAgentStylesheet() {
    return Objects.requireNonNull(XxxControl.class.getResource("xxx-control.css")).toExternalForm();
}

@Override
protected Skin<?> createDefaultSkin() {
    return new XxxControlSkin(this);
}
```

### Package Layout

| Package                      | Purpose                                                         |
|------------------------------|-----------------------------------------------------------------|
| `com.dlsc.gemsfx`            | All public control classes                                      |
| `com.dlsc.gemsfx.skins`      | All skin implementations (also exported)                        |
| `com.dlsc.gemsfx.binding`    | Reusable `ObjectBinding` subclasses for nested/aggregated lists |
| `com.dlsc.gemsfx.daterange`  | Date range picker and view                                      |
| `com.dlsc.gemsfx.infocenter` | Notification center pane and model classes                      |
| `com.dlsc.gemsfx.paging`     | Paging list/table view controls                                 |
| `com.dlsc.gemsfx.gridtable`  | Grid-table control                                              |
| `com.dlsc.gemsfx.treeview`   | Tree node view                                                  |
| `com.dlsc.gemsfx.incubator`  | Experimental controls (no stability guarantee)                  |
| `com.dlsc.gemsfx.util`       | Utilities (SessionManager, HistoryManager, converters, etc.)    |

### Module

The library is an **open** JPMS module (`open module com.dlsc.gemsfx`). All sub-packages listed above are explicitly
`exports`-ed.

### Demo Apps

Each control has a dedicated `*App.java` in `gemsfx-demo/src/main/java/com/dlsc/gemsfx/demo/`. Demo apps extend
`Application` and are self-contained. Use `-Dmain.class=com.dlsc.gemsfx.demo.XxxApp` to run them.

## Key Conventions

### Listener Lifecycle

The codebase heavily uses `WeakChangeListener`, `WeakListChangeListener`, and `WeakInvalidationListener` to avoid memory
leaks — prefer these over strong listeners whenever a skin or control holds a listener on an observable it doesn't own.
Strong listeners must be removed in `Skin.dispose()`.

### CSS Naming

CSS style classes and file names use **kebab-case** derived from the Java class name (e.g., `FilterView` →
`"filter-view"` style class and `filter-view.css`). CSS selectors inside those files are scoped under the root style
class.

### Properties

Controls expose JavaFX properties using the standard JavaFX bean pattern: a backing `*Property` field, a getter, a
setter, and a `*Property()` accessor. Read-only properties use `ReadOnlyXxxWrapper` internally and expose
`ReadOnlyXxxProperty` publicly.

### Icons

Icons are provided by [Ikonli](https://kordamp.org/ikonli/) (`org.kordamp.ikonli`). The demo and library use
MaterialDesign, Material, and Bootstrap icon packs. Use `FontIcon` from `ikonli-javafx`.

### Key Dependencies

- `org.apache.commons.lang3` — `StringUtils`, etc.
- `commons-validator` — email validation in `EmailField`
- `net.synedra.validatorfx` — form validation support
- `org.controlsfx.controls` — used internally for some popups
- `com.github.weisj.jsvg` — SVG rendering in `SVGImageView`
- `com.dlsc.pickerfx` / `com.dlsc.unitfx` — sibling DLSC libraries
