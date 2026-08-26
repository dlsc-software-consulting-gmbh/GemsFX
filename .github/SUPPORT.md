# Support

Thanks for using GemsFX! Here is where to go depending on what you need.

## Documentation first

- The [README](../README.md) documents every control, the dependency coordinates and how to run the
  demos.
- The [website](https://gemsfx.dlsc.com) hosts the Javadoc and the CSS reference.
- The PDF manuals in [`docs/manuals`](../docs/manuals) describe individual controls in detail. They
  are also browsable from within the showcase application:
  ```bash
  mvn javafx:run -f gemsfx-showcase/pom.xml
  ```
- Each control has a runnable demo application:
  ```bash
  mvn javafx:run -f gemsfx-demo/pom.xml -Dmain.class=com.dlsc.gemsfx.demo.FilterViewApp
  ```

## Questions and ideas

Use [GitHub Discussions](https://github.com/dlsc-software-consulting-gmbh/GemsFX/discussions) for

- "how do I ...?" questions,
- usage and styling help,
- sharing what you built with GemsFX,
- discussing ideas before they become a feature request.

Please do **not** use the issue tracker for questions.

## Bugs and feature requests

Use the [issue tracker](https://github.com/dlsc-software-consulting-gmbh/GemsFX/issues) and pick one
of the templates:

- **Bug report** – please attach a minimal, self-contained JavaFX application that reproduces the
  problem, together with the GemsFX, Java, JavaFX and operating system versions.
- **Feature request** – describe the problem you want to solve, not only the solution you have in
  mind.

## Security issues

Never report security problems in public. Follow the instructions in [SECURITY.md](SECURITY.md).

## Contributing

Want to fix it yourself? Great — see [CONTRIBUTING.md](CONTRIBUTING.md).

## Commercial support

GemsFX is developed and maintained by
[DLSC Software &amp; Consulting GmbH](https://www.dlsc.com). If you need dedicated support,
custom controls, training or consulting around JavaFX, get in touch via
[dlsc.com](https://www.dlsc.com).

Community support is provided on a best-effort basis by volunteers; there is no service level
agreement for issues or discussions.
