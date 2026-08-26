# Description

<!-- What does this pull request change and why? -->

## Related issue

<!-- e.g. Fixes #123 / Closes #123 / Relates to #123 -->

## Type of change

- [ ] Bug fix
- [ ] New control
- [ ] New feature for an existing control
- [ ] Styling / CSS change
- [ ] Accessibility improvement
- [ ] Localization
- [ ] Documentation
- [ ] Build / infrastructure

## Screenshots

<!-- For UI changes, please add before/after screenshots or a short screen recording. -->

## Checklist

- [ ] I have read the [contributing guidelines](https://github.com/dlsc-software-consulting-gmbh/GemsFX/blob/master/.github/CONTRIBUTING.md).
- [ ] `./mvnw -B verify` passes locally.
- [ ] Changes to the `gemsfx` module still compile against **Java 11** and **JavaFX 17**.
- [ ] New or changed public API is fully documented with Javadoc (property Javadoc on the
      `xxxProperty()` accessor, property methods declared `final`).
- [ ] New or changed behaviour is covered by tests (extending `FxTestBase` when the toolkit is
      needed).
- [ ] New controls set an `AccessibleRole` via `AccessibilityUtil` and are registered in
      `AccessibilityTest`.
- [ ] New user-visible texts go through `ResourceBundleManager` and were added to the existing
      locale bundles.
- [ ] New controls come with a demo application in `gemsfx-demo` and a section in `README.md`.
- [ ] Listeners on foreign observables are weak or removed in `Skin.dispose()`.
- [ ] I agree that my contribution is licensed under the [Apache License 2.0](https://github.com/dlsc-software-consulting-gmbh/GemsFX/blob/master/LICENSE).

## Additional notes

<!-- Anything reviewers should know: breaking changes, open questions, follow-up work. -->
