# Camunda Test Parallel - Usage Guide

A library for enabling parallel execution of Spring Boot integration tests with embedded Camunda 7 engines.

## Problem Statement

Spring Boot integration tests with Camunda typically run sequentially because:
- All tests share the same database schema
- WireMock servers use fixed ports, causing conflicts

This creates significant bottlenecks in CI/CD pipelines and local development.

## Solution

The `@ParallelFunctionalTest` annotation provides:
- **Schema Isolation**: Each test class gets a unique PostgreSQL schema
- **Dynamic Port Allocation**: WireMock ports are automatically allocated and injected
- **Parallel Execution**: Multiple test classes can run concurrently without conflicts

## Quick Start

### 1. Add Dependency

```kotlin
// build.gradle.kts
dependencies {
    testImplementation("io.codepush.testing:camunda-test-parallel:0.1.0-SNAPSHOT")
}
```

### 2. Basic Usage

Replace `@SpringBootTest` with `@ParallelFunctionalTest`:

```java
@ParallelFunctionalTest
class ProcessDeploymentTest {

    @Autowired
    private ProcessEngine processEngine;

    @Test
    void shouldDeployProcess() {
        // Test runs in isolated schema: camunda_test_<uuid>
        // WireMock ports are dynamically allocated
    }
}
```

### 3. Custom Properties

Add custom properties just like with `@SpringBootTest`:

```java
@ParallelFunctionalTest(
    properties = {
        "custom.feature.enabled=true",
        "custom.timeout=5000"
    }
)
class CustomFeatureTest {

    @Test
    void shouldUseCustomConfiguration() {
        // Custom properties merged with dynamic configuration
    }
}
```

## How It Works

### Schema Isolation

Each test class automatically gets:
- A unique schema name: `camunda_test_<uuid>`
- Injected via property: `spring.datasource.schema`
- Automatically cleaned up after tests complete

Access the schema name in your tests:

```java
@Value("${spring.datasource.schema}")
private String schemaName;
```

### Dynamic Port Allocation

By default, 3 ports are allocated and available as:
- `${wiremock.port.0}`
- `${wiremock.port.1}`
- `${wiremock.port.2}`

Use in your configuration:

```java
@Value("${wiremock.port.0}")
private int externalApiPort;

@TestConfiguration
static class WireMockConfig {
    @Bean
    WireMockServer externalApiMock(@Value("${wiremock.port.0}") int port) {
        return new WireMockServer(port);
    }
}
```

Or in YAML:

```yaml
external-api:
  base-url: http://localhost:${wiremock.port.0}
```

## Configuration

### Database Setup

Ensure your test configuration includes PostgreSQL connection:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/camunda_test
    username: test_user
    password: test_password
```

The schema will be dynamically injected by the framework.

### Parallel Execution

Enable JUnit parallel execution in `junit-platform.properties`:

```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
junit.jupiter.execution.parallel.mode.classes.default=concurrent
```

## Migration from @FunctionalTest

If you have an existing `@FunctionalTest` annotation:

```java
// Before
@FunctionalTest
class MyTest { }

// After
@ParallelFunctionalTest
class MyTest { }
```

All existing properties and configuration are preserved.

## Example

```java
@ParallelFunctionalTest(
    properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/test_db",
        "spring.datasource.username=test",
        "spring.datasource.password=test"
    }
)
class OrderProcessTest {

    @Autowired
    private RuntimeService runtimeService;

    @Value("${wiremock.port.0}")
    private int paymentServicePort;

    @Test
    void shouldProcessOrder() {
        // Runs in isolated schema
        // Payment service mock on dynamic port
        ProcessInstance instance = runtimeService
            .startProcessInstanceByKey("orderProcess");

        assertThat(instance).isNotNull();
    }
}
```

## Benefits

- **Faster Test Execution**: Run tests in parallel instead of sequentially
- **No Port Conflicts**: Dynamic allocation prevents WireMock port collisions
- **Schema Isolation**: Each test has its own clean database schema
- **Easy Migration**: Simple annotation swap from existing tests
- **Automatic Cleanup**: Resources cleaned up after test completion

## Requirements

- Java 21+
- Spring Boot 3.2+
- PostgreSQL database
- JUnit 5

## Limitations

- PostgreSQL only (currently)
- Shared database container (separate schemas)
- Fixed number of WireMock ports (3 by default)

## Troubleshooting

### Schema not found

Ensure your PostgreSQL user has permission to create schemas:

```sql
GRANT CREATE ON DATABASE your_db TO your_user;
```

### Port allocation failures

If you see port allocation errors, check:
- No firewalls blocking ephemeral port range (49152-65535)
- Sufficient ports available
- No other processes consuming ports rapidly

### Tests still running sequentially

Verify JUnit parallel execution is enabled in `junit-platform.properties`.

## Support

For issues and questions: https://github.com/codepush-io/camunda-test-parallel/issues
