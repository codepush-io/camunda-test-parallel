# Camunda Parallel Test Support

[![Java Version](https://img.shields.io/badge/Java-21+-blue.svg)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/codepush-io/camunda-test-parallel)

A Java library that enables parallel execution of Spring Boot integration tests with embedded Camunda runtime. This library solves the challenge of running Camunda integration tests concurrently by providing the necessary infrastructure and utilities for test isolation.

## Problem Statement

Spring Boot integration tests with embedded Camunda runtime typically cannot run in parallel due to shared resources, database connections, and process engine state. This significantly increases CI/CD pipeline duration and slows down development feedback loops.

## Features

- **Parallel Test Execution**: Run multiple Camunda integration tests concurrently
- **Resource Isolation**: Automatic isolation of database connections and process engine instances
- **Spring Boot Integration**: Seamless integration with Spring Boot test infrastructure
- **Easy Configuration**: Minimal configuration required to enable parallel testing
- **Camunda 8 Support**: Built for modern Camunda Platform 8

> **Note**: This library is currently in early development. Features are being actively implemented.

## Requirements

- **Java 21+**: Takes advantage of modern Java features and virtual threads
- **Spring Boot 3.x**: Compatible with Spring Boot 3.x series
- **Camunda 8.x**: Designed for Camunda Platform 8
- **Gradle 8+** or **Maven 3.6+**: For building projects using this library

## Getting Started

### Using DevContainers (Recommended)

The fastest way to get started with development is using DevContainers:

1. **Prerequisites**:
   - Install [Docker](https://www.docker.com/products/docker-desktop)
   - Install [Visual Studio Code](https://code.visualstudio.com/)
   - Install the [Dev Containers extension](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers)

2. **Open in DevContainer**:
   ```bash
   git clone https://github.com/codepush-io/camunda-test-parallel.git
   cd camunda-test-parallel
   code .
   ```
   - When prompted, click **"Reopen in Container"**
   - Alternatively, use the command palette: `Dev Containers: Reopen in Container`

3. **Wait for initialization**: The container will automatically build and configure the development environment with Java 21, Gradle, and all necessary tools.

### Local Development Setup

If you prefer to develop without DevContainers:

1. **Install Java 21**:
   - Download from [Adoptium](https://adoptium.net/) or use your preferred JDK distribution
   - Verify installation: `java -version`

2. **Clone the repository**:
   ```bash
   git clone https://github.com/codepush-io/camunda-test-parallel.git
   cd camunda-test-parallel
   ```

3. **Build the project**:
   ```bash
   ./gradlew build
   ```

## Building

### Build the library

```bash
./gradlew build
```

### Run tests

Run all tests:
```bash
./gradlew check
```

Run only unit tests:
```bash
./gradlew test
```

Run only integration tests:
```bash
./gradlew integrationTest
```

### Generate artifacts

Generate JAR files (including sources and javadoc):
```bash
./gradlew assemble
```

### Clean build

```bash
./gradlew clean build
```

## Usage

> **Coming Soon**: Usage examples and documentation will be added as the library features are implemented.

### Basic Example

```java
// Example usage will be provided once core features are implemented
```

## Project Structure

```
camunda-test-parallel/
├── src/
│   ├── main/
│   │   ├── java/              # Library source code
│   │   └── resources/         # Library resources
│   ├── test/
│   │   ├── java/              # Unit tests
│   │   └── resources/         # Test resources
│   └── integrationTest/
│       ├── java/              # Integration tests
│       └── resources/         # Integration test resources
├── .devcontainer/             # DevContainer configuration
├── build.gradle.kts           # Gradle build configuration
├── settings.gradle.kts        # Gradle settings
└── README.md
```

## Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details on:

- Setting up your development environment
- Code style and standards
- Submitting pull requests
- Reporting issues

## Roadmap

- [ ] Core parallel execution infrastructure
- [ ] Database connection isolation
- [ ] Process engine instance isolation
- [ ] Spring Boot test annotations
- [ ] Configuration options
- [ ] Comprehensive documentation
- [ ] Example projects

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Support

- **Issues**: Report bugs or request features via [GitHub Issues](https://github.com/codepush-io/camunda-test-parallel/issues)
- **Discussions**: Join the conversation in [GitHub Discussions](https://github.com/codepush-io/camunda-test-parallel/discussions)
- **Contributing**: See [CONTRIBUTING.md](CONTRIBUTING.md) for how to get involved

## Acknowledgments

This project is inspired by the need for faster feedback loops in Camunda-based applications and builds upon the excellent work of the Spring Boot and Camunda communities.
