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
- **Camunda 7 Support**: Built for Camunda BPM Platform 7 embedded in Spring Boot

> **Note**: This library is currently in early development. Features are being actively implemented.

## Requirements

- **Java 21+**: Takes advantage of modern Java features and virtual threads
- **Spring Boot 3.x**: Compatible with Spring Boot 3.x series
- **Camunda 7.x**: Designed for Camunda BPM Platform 7 embedded in Spring Boot
- **Gradle 8+** or **Maven 3.6+**: For building projects using this library

## Quick Start

### 1. Add the dependency

```kotlin
// build.gradle.kts
dependencies {
    testImplementation("io.codepush.testing:camunda-test-parallel:0.1.0-SNAPSHOT")
}
```

### 2. Replace `@SpringBootTest` with `@ParallelFunctionalTest`

```java
@ParallelFunctionalTest
class OrderProcessTest {

    @Autowired
    private RuntimeService runtimeService;

    @Value("${wiremock.port.0}")
    private int paymentServicePort;

    @Test
    void shouldProcessOrder() {
        // Runs in isolated schema (camunda_test_<uuid>)
        // WireMock port dynamically allocated — no conflicts
        ProcessInstance instance = runtimeService
            .startProcessInstanceByKey("orderProcess");

        assertThat(instance).isNotNull();
    }
}
```

That's it. Each test class automatically gets:
- A unique PostgreSQL schema (`camunda_test_<uuid>`) created before tests and dropped after
- 3 dynamically allocated ports available as `${wiremock.port.0}`, `${wiremock.port.1}`, `${wiremock.port.2}`
- Spring Boot test context with `RANDOM_PORT` web environment and `test` profile

### 3. Enable JUnit parallel execution

Add `src/test/resources/junit-platform.properties`:

```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
junit.jupiter.execution.parallel.mode.classes.default=concurrent
```

### 4. Configure your test database

```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/camunda_test
    username: test_user
    password: test_password
```

The library creates and drops isolated schemas automatically — your PostgreSQL user just needs `CREATE` permission on the database.

## Configuration

### Custom properties

Pass additional Spring properties just like `@SpringBootTest`:

```java
@ParallelFunctionalTest(
    properties = {
        "custom.feature.enabled=true",
        "custom.timeout=5000"
    }
)
class CustomFeatureTest { }
```

### WireMock port count

Control how many ports are allocated per test class (default is 3):

```java
// Allocate 5 ports: ${wiremock.port.0} through ${wiremock.port.4}
@ParallelFunctionalTest(wiremockPorts = 5)
class ManyExternalServicesTest { }

// Disable port allocation entirely
@ParallelFunctionalTest(wiremockPorts = 0)
class NoDependenciesTest { }
```

### Using allocated ports with WireMock

```java
@ParallelFunctionalTest
class PaymentServiceTest {

    @Value("${wiremock.port.0}")
    private int paymentPort;

    private WireMockServer paymentMock;

    @BeforeEach
    void setUp() {
        paymentMock = new WireMockServer(paymentPort);
        paymentMock.start();
    }

    @AfterEach
    void tearDown() {
        paymentMock.stop();
    }

    @Test
    void shouldCallPaymentService() {
        paymentMock.stubFor(post("/charge")
            .willReturn(ok()));
        // test logic...
    }
}
```

Or in your application config:

```yaml
# application-test.yml
payment-service:
  base-url: http://localhost:${wiremock.port.0}
notification-service:
  base-url: http://localhost:${wiremock.port.1}
```

### Accessing the isolated schema name

```java
@Value("${spring.datasource.schema}")
private String schemaName;  // e.g. "camunda_test_a1b2c3d4-..."
```

## How It Works

1. **Before each test class**, the `ParallelTestExecutionListener`:
   - Creates a unique PostgreSQL schema (`camunda_test_<uuid>`)
   - Allocates dynamic ports by binding to port 0
   - Injects schema name and ports into the test's Spring `ApplicationContext` via `TestPropertyValues` (context-isolated, not global)

2. **After each test class**, the listener:
   - Drops the schema (`DROP SCHEMA ... CASCADE`)
   - Releases the allocated ports

All state is per-`ApplicationContext` — no `System.setProperty` pollution, no cross-test interference.

## Migration from `@FunctionalTest` or `@SpringBootTest`

```java
// Before
@SpringBootTest
class MyTest { }

// After
@ParallelFunctionalTest
class MyTest { }
```

Existing properties and configuration are preserved. The annotation composes `@SpringBootTest(webEnvironment = RANDOM_PORT)`, `@EnableAutoConfiguration`, and `@ActiveProfiles("test")`.

## Development Setup

### Using DevContainers (Recommended)

1. Install [Docker](https://www.docker.com/products/docker-desktop) and [VS Code](https://code.visualstudio.com/) with the [Dev Containers extension](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers)
2. Clone and open:
   ```bash
   git clone https://github.com/codepush-io/camunda-test-parallel.git
   cd camunda-test-parallel
   code .
   ```
3. Click **"Reopen in Container"** when prompted

### Local Setup

1. Install [Java 21](https://adoptium.net/)
2. Clone and build:
   ```bash
   git clone https://github.com/codepush-io/camunda-test-parallel.git
   cd camunda-test-parallel
   ./gradlew build
   ```

### Running tests

```bash
./gradlew test              # Unit tests
./gradlew integrationTest   # Integration tests
./gradlew check             # All tests
```

## Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details on:

- Setting up your development environment
- Code style and standards
- Submitting pull requests
- Reporting issues

## Roadmap

- [x] Core parallel execution infrastructure
- [x] Database schema isolation (PostgreSQL)
- [x] Spring Boot test annotations (`@ParallelFunctionalTest`)
- [x] Dynamic WireMock port allocation
- [x] Context-isolated property injection
- [x] Schema-aware DataSource wrapper
- [x] Configurable WireMock port count
- [ ] Process engine instance isolation
- [ ] Testcontainers auto-provisioning
- [ ] Support for databases beyond PostgreSQL
- [ ] Example projects

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Support

- **Issues**: Report bugs or request features via [GitHub Issues](https://github.com/codepush-io/camunda-test-parallel/issues)
- **Discussions**: Join the conversation in [GitHub Discussions](https://github.com/codepush-io/camunda-test-parallel/discussions)
- **Contributing**: See [CONTRIBUTING.md](CONTRIBUTING.md) for how to get involved

## Acknowledgments

This project is inspired by the need for faster feedback loops in Camunda-based applications and builds upon the excellent work of the Spring Boot and Camunda communities.
