# Security Policy

## Supported versions

GemsFX is a UI component library. Security fixes are only provided for the latest released version
on Maven Central.

| Version              | Supported          |
|----------------------|--------------------|
| Latest `4.x` release | :white_check_mark: |
| Older releases       | :x:                |

If you are affected by a vulnerability, please upgrade to the most recent release first.

## Reporting a vulnerability

**Please do not report security vulnerabilities through public GitHub issues, discussions or pull
requests.**

Instead, report them privately in one of the following ways:

1. **GitHub private vulnerability reporting** – use the *"Report a vulnerability"* button on the
   [Security tab](https://github.com/dlsc-software-consulting-gmbh/GemsFX/security) of this
   repository (preferred, if available).
2. **Email** – send a description to **dlemmermann@gmail.com** with `GemsFX security` in the
   subject line.

Please include as much of the following information as possible:

- the affected GemsFX version (and Java / JavaFX version and operating system),
- the type of issue and the component or control involved,
- a description of the impact and how an attacker could exploit it,
- step-by-step instructions or a minimal application that reproduces the problem,
- any proposed fix or mitigation you are aware of.

## What to expect

- We aim to acknowledge your report within **five business days**.
- We will keep you informed about the progress of the analysis and the fix.
- Once a fix is released, we will credit you in the release notes unless you prefer to stay
  anonymous.

Please give us a reasonable amount of time to release a fix before disclosing the issue publicly.

## Scope

This policy covers the code published in this repository, in particular the `gemsfx` library module.
Issues in third-party dependencies should be reported to the respective projects; feel free to open
a regular issue here so that we can upgrade the affected dependency.
