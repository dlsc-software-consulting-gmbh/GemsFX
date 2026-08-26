# Contributing to GemsFX

Thanks for your interest in GemsFX! This library lives from the controls, fixes and ideas that the
JavaFX community contributes. This document explains how to build the project, which conventions the
code follows and what we expect from a pull request.

By participating in this project you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md).

## Table of contents

- [Ways to contribute](#ways-to-contribute)
- [Project layout](#project-layout)
- [Building and testing](#building-and-testing)
- [Running the demos and the showcase](#running-the-demos-and-the-showcase)
- [Coding conventions](#coding-conventions)
- [Adding a new control](#adding-a-new-control)
- [Internationalization](#internationalization)
- [Pull request workflow](#pull-request-workflow)
- [License of contributions](#license-of-contributions)

## Ways to contribute

- **Report a bug** – use the [bug report form](https://github.com/dlsc-software-consulting-gmbh/GemsFX/issues/new?template=bug_report.yml).
  Please include a minimal, self-contained JavaFX application that reproduces the problem.
- **Request a feature or a new control** – use the [feature request form](https://github.com/dlsc-software-consulting-gmbh/GemsFX/issues/new?template=feature_request.yml).
- **Ask a question** – please use [GitHub Discussions](https://github.com/dlsc-software-consulting-gmbh/GemsFX/discussions)
  instead of the issue tracker (see [SUPPORT.md](SUPPORT.md)).
- **Improve documentation** – Javadoc, README sections and control manuals are all fair game.
- **Send a pull request** – see the [workflow](#pull-request-workflow) below.

For larger changes (a new control, an API change, a refactoring that touches many files) please open
an issue first so that we can agree on the direction before you invest a lot of time.

## Project layout

| Module            | Purpose                                                                              |
|-------------------|--------------------------------------------------------------------------------------|
| `gemsfx`          | The library itself, published as `com.dlsc.gemsfx:gemsfx`. Targets **Java 11** / **JavaFX 17**. |
| `gemsfx-demo`     | One standalone demo application per control. Targets **Java 24** / **JavaFX 25**.     |
| `gemsfx-showcase` | Application listing all controls with their PDF manuals from `docs/manuals`.          |

Packages inside the `gemsfx` module:

| Package                      | Purpose                                                          |
|------------------------------|------------------------------------------------------------------|
| `com.dlsc.gemsfx`            | Public control classes                                            |
| `com.dlsc.gemsfx.skins`      | Skin implementations                                              |
| `com.dlsc.gemsfx.binding`    | Reusable `ObjectBinding` subclasses for nested/aggregated lists    |
| `com.dlsc.gemsfx.daterange`  | Date range picker and view                                        |
| `com.dlsc.gemsfx.infocenter` | Notification center pane and model classes                        |
| `com.dlsc.gemsfx.paging`     | Paging list/table view controls                                   |
| `com.dlsc.gemsfx.gridtable`  | Grid table control                                                |
| `com.dlsc.gemsfx.treeview`   | Tree node view                                                    |
| `com.dlsc.gemsfx.incubator`  | Experimental controls, no stability guarantee                     |
| `com.dlsc.gemsfx.util`       | Utilities (`SessionManager`, `HistoryManager`, converters, ...)   |

The library is an **open JPMS module** (`open module com.dlsc.gemsfx`); new packages that are meant
to be public API must be added to `module-info.java`.

> **Important:** the `gemsfx` module must keep compiling against **Java 11** and **JavaFX 17**.
> Do not use language features or APIs that are only available in newer releases inside that module.
> The demo and showcase modules may use modern Java.

## Building and testing

The project uses the Maven wrapper, so no local Maven installation is required. A JDK 24 (or newer)
is needed to build all modules.

```bash
# build everything (compile, test, javadoc, package)
./mvnw -B verify

# run only the tests of the library module
./mvnw test -pl gemsfx

# run a single test class
./mvnw test -pl gemsfx -Dtest=CalendarViewTest
```

The unit tests create real controls and therefore need a running JavaFX toolkit. They run headless
on the [Monocle](https://github.com/TestFX/Monocle) platform; the required system properties are
already configured for the surefire plugin in `gemsfx/pom.xml`, so no Xvfb or display is needed.

Tests that need the toolkit extend `com.dlsc.gemsfx.FxTestBase`, which starts the toolkit once per
JVM and offers helpers to run code on the JavaFX application thread (`runFx`, `invoke`) and to force
skin creation (`layout`).

Please make sure that `./mvnw -B verify` passes locally before opening a pull request. The same
command runs on CI.

## Running the demos and the showcase

```bash
# run the demo application of a single control
mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.FilterViewApp

# run the showcase application (all controls plus their manuals)
mvn javafx:run -f gemsfx-showcase/pom.xml
```

## Coding conventions

### Control – Skin – CSS

Every control follows the standard JavaFX MVC split:

1. **Control class** `com.dlsc.gemsfx.XxxControl` extending `Control` (or `Region`)
2. **Skin class** `com.dlsc.gemsfx.skins.XxxControlSkin` extending `SkinBase<XxxControl>`
3. **CSS file** `gemsfx/src/main/resources/com/dlsc/gemsfx/xxx-control.css`

All three pieces are wired together in the control:

```java
// in the constructor
getStyleClass().add("xxx-control");

@Override
public String getUserAgentStylesheet() {
    return Objects.requireNonNull(XxxControl.class.getResource("xxx-control.css")).toExternalForm();
}

@Override
protected Skin<?> createDefaultSkin() {
    return new XxxControlSkin(this);
}
```

### CSS naming

Style classes and CSS file names use **kebab-case** derived from the Java class name
(`FilterView` → style class `filter-view`, file `filter-view.css`). Selectors inside a file are
scoped under the root style class of the control.

### Properties

- Use the standard JavaFX bean pattern: backing `*Property` field, getter, setter and a
  `*Property()` accessor.
- **All property methods (`xxxProperty()`, getter and setter) must be `final`.**
- Read-only properties use a `ReadOnlyXxxWrapper` internally and expose a `ReadOnlyXxxProperty`.
- The **Javadoc of a property belongs on the `xxxProperty()` accessor only**; getter and setter
  refer to it with `@see`/`{@link}` instead of duplicating the description.
- Styleable properties use `StyleableObjectProperty` / `CssMetaData` following the pattern of the
  existing controls.

### Listener lifecycle

The codebase uses `WeakChangeListener`, `WeakListChangeListener` and `WeakInvalidationListener`
extensively to avoid memory leaks. Prefer them whenever a skin or a control registers a listener on
an observable that it does not own. Strong listeners must be removed in `Skin.dispose()`.

### Accessibility

- Every user-facing control sets an appropriate `AccessibleRole` in its constructor and, where it
  carries a value, derives its `accessibleText` from its own state.
- Use `com.dlsc.gemsfx.util.AccessibilityUtil`: `setRole(node, role[, roleDescription])` and
  `bindAccessibleText(node, observable)`.
- Set the role in the most specific control class; do not re-set it in subclasses that only inherit.
- Accessibility strings are localized through `ResourceBundleManager` using the control's
  `BundleType` and the keys `accessible.role-description` / `accessible.text.*`, with the English
  text as fallback. Resolve patterns once at construction time, not on every value change.
- New controls must be registered in `AccessibilityTest`, which verifies the role of each control.

### Javadoc

The published `gemsfx` module is held to a strict documentation standard enforced by DocLint
(`<doclint>all</doclint>` in the root `pom.xml`). Every public and protected type, method,
constructor and field should be documented. Javadoc **errors** (for example broken `{@link}`
references) break the build.

### Icons and dependencies

- Icons come from [Ikonli](https://kordamp.org/ikonli/); use `FontIcon` from `ikonli-javafx`
  (MaterialDesign, Material and Bootstrap packs are available).
- Prefer the dependencies that are already part of the project (`commons-lang3`,
  `commons-validator`, `validatorfx`, `controlsfx`, `jsvg`, `pickerfx`, `unitfx`) over adding new
  ones. New third-party dependencies need to be discussed in an issue first, as they become part of
  the public dependency tree of the library.

### General style

- Follow the formatting of the surrounding code (4 spaces, no tabs).
- Add comments only where the code needs clarification.
- Keep pull requests focused; avoid unrelated reformatting.

## Adding a new control

When you contribute a completely new control, please include:

1. The control class, its skin and its CSS file (see the conventions above).
2. `module-info.java` updates if a new package is introduced.
3. Full Javadoc for the public API and an `AccessibleRole`.
4. Unit tests extending `FxTestBase`, plus an entry in `AccessibilityTest`.
5. A demo application `gemsfx-demo/src/main/java/com/dlsc/gemsfx/demo/XxxApp.java`.
6. A short section in `README.md` (with an anchor and, ideally, a screenshot in `gemsfx/docs`).
7. If a PDF manual is added under `docs/manuals`, an entry in `ShowcaseRegistry`.

## Internationalization

All built-in localized texts of the `gemsfx` module go through `ResourceBundleManager`
(`com.dlsc.gemsfx.util`). To add a new translatable string:

1. Identify the target bundle from `ResourceBundleManager.BundleType`.
2. Add the key and the default text to the base bundle `<bundle>.properties`
   (in `gemsfx/src/main/resources`).
3. Add the same key to all existing locale files of that bundle
   (`<bundle>_de.properties`, `<bundle>_zh.properties`, ...).
4. Look the value up in code:
   ```java
   ResourceBundleManager.getString(ResourceBundleManager.BundleType.DIALOG_PANE, "button.send", "Send");
   ```
   Use `ResourceBundleManager.format(...)` when arguments are involved.
5. If a new bundle domain is required, add a new `BundleType` constant and the corresponding
   property files.

Bundle names use kebab-case (`duration-picker.properties`), keys use lowercase dot notation grouped
by feature and intent (`action.clear`, `placeholder.no-items`, `unit.long.minutes`).

The `gemsfx-demo` module is intentionally out of scope for internationalization.

## Pull request workflow

1. Fork the repository and create a topic branch from `master`.
2. Make your change, keeping commits focused and with descriptive messages.
3. Run `./mvnw -B verify` and make sure the build and all tests pass.
4. Push your branch and open a pull request against `master`, filling in the pull request template.
5. Link the issue your change relates to (`Fixes #123`).
6. Be ready to iterate on review feedback. Maintainers may ask for API changes to keep the library
   consistent.

CI (GitHub Actions) must be green before a pull request can be merged.

## License of contributions

GemsFX is licensed under the [Apache License 2.0](../LICENSE). By submitting a pull request you
agree that your contribution is licensed under the same terms.
