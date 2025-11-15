Upgrade to Java 21 (LTS) — Guide

Goal
- Build and run tests using Java 21.

Summary
- A Maven POM (`pom.xml`) was added and configured to use Java 21 for compilation and to include JUnit Jupiter for tests.

Prerequisites (choose one)
- Install JDK 21 (Adoptium/Temurin or another vendor).
  - Homebrew (macOS):

```bash
brew install --cask temurin21
```

  - SDKMAN (macOS/Linux):

```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.0-tem
```

  - Or download from https://adoptium.net or your preferred vendor.

- Install Maven (if not installed):

```bash
brew install maven
```

What I changed
- Added `pom.xml` at project root configured for Java 21 and JUnit 5.

How to build & run tests with Java 21
1. Ensure `java -version` shows a Java 21 runtime. If you have multiple JDKs, set JAVA_HOME to the JDK 21 installation path.

Example (macOS, Homebrew Temurin):

```bash
export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

2. From the repository root run (Maven will download JUnit automatically):

```bash
mvn clean test
```

Notes & next steps
- If you prefer Gradle or an IDE config, I can add a Gradle wrapper that targets Java 21.
- If you want automated testing in CI, I can add a GitHub Actions workflow that installs JDK 21 and runs `mvn test`.
- I attempted to run the automated upgrade tooling to produce an OpenRewrite-based patch, but that tool isn't available in this environment; this manual POM is a practical alternative to target Java 21.
