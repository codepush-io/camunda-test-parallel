p# Initial Repository Setup

This prompt establishes the foundational structure for the Camunda Parallel Test library - a Java library that enables parallel execution of Spring Boot integration tests with embedded Camunda runtime. The setup focuses on creating a developer-friendly, contribution-ready environment using DevContainers and Gradle, ensuring consistency across development environments and streamlining the onboarding process for new contributors.

## Requirements

- Create a DevContainer configuration that includes Java 21, Gradle, and all necessary development tools
- Set up a Gradle-based Java library project with appropriate directory structure following standard conventions
- Configure Gradle build files for a library project (not an application)
- Create a comprehensive README.md that explains the project purpose, setup instructions, and contribution guidelines
- Establish a logical directory structure for source code, tests, and documentation
- Include basic Gradle tasks for building, testing, and publishing the library
- Configure Git ignore patterns appropriate for Java/Gradle projects

## Rules

*No specific rules files required for this initial setup task*

## Domain

```java
// Project Structure
Project {
  - src/main/java/          // Library source code
  - src/test/java/          // Unit tests
  - src/integrationTest/java/  // Integration tests
  - build.gradle.kts        // Gradle build configuration
  - settings.gradle.kts     // Gradle settings
  - .devcontainer/          // DevContainer configuration
    - devcontainer.json
    - Dockerfile (if needed)
  - .gitignore
  - README.md
  - prompts/                // Project prompts (already exists)
}

// Library Purpose
CamundaParallelTestSupport {
  purpose: "Enable parallel execution of Spring Boot integration tests with embedded Camunda"
  target: "Spring Boot applications using embedded Camunda runtime"
  problem: "Integration tests with embedded Camunda cannot run in parallel out-of-the-box"
}
```

## Extra Considerations

- Use Java 21 as the baseline Java version for modern language features and long-term support
- Ensure DevContainer includes Git configuration and common development utilities
- Consider including Maven wrapper alternative or stick exclusively to Gradle
- Set up Gradle with Kotlin DSL (build.gradle.kts) for better IDE support and type safety
- Include standard EditorConfig for consistent code formatting across different IDEs
- Consider including a CONTRIBUTING.md file for contributor guidelines
- Set up a LICENSE file (consider Apache 2.0 or MIT for library projects)

## Testing Considerations

- Configure a separate source set for integration tests in Gradle
- Ensure Gradle test tasks can be run both locally and in CI/CD pipelines
- Include JUnit 5 (Jupiter) as the testing framework
- Consider including AssertJ or similar for fluent assertions
- Set up test logging configuration for better debugging

## Implementation Notes

- Use Gradle Kotlin DSL for all build files
- Follow standard Gradle project layout conventions
- DevContainer should be based on a standard Java development container
- Include useful VS Code extensions in DevContainer for Java development (Extension Pack for Java, Gradle for Java, etc.)
- README should include badges for build status (placeholder), license, and Java version
- Use semantic versioning for the library (start at 0.1.0-SNAPSHOT)
- Configure Gradle to generate source and javadoc JARs for publishing

## Specification by Example

### README.md Structure
```markdown
# Camunda Parallel Test Support

Brief description of the library and its purpose

## Features
- Feature 1 (to be implemented)
- Feature 2 (to be implemented)

## Requirements
- Java 21+
- Spring Boot 3.x
- Camunda 8.x

## Getting Started

### Using DevContainers (Recommended)
Instructions for opening in DevContainer

### Local Development
Instructions for local setup without DevContainer

## Building
gradle build commands

## Contributing
Link to CONTRIBUTING.md

## License
License information
```

### DevContainer Configuration
```json
{
  "name": "Camunda Parallel Test - Java 21",
  "image": "mcr.microsoft.com/devcontainers/java:21",
  "features": {
    "ghcr.io/devcontainers/features/gradle:1": {}
  },
  "customizations": {
    "vscode": {
      "extensions": [
        "vscjava.vscode-java-pack",
        "vscjava.vscode-gradle"
      ]
    }
  }
}
```

### Gradle Build Structure
```kotlin
// build.gradle.kts
plugins {
    `java-library`
    `maven-publish`
}

group = "io.github.camunda"
version = "0.1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    // Spring Boot and Camunda dependencies
    // Test dependencies
}

// Integration test source set configuration
// Publishing configuration
```

## Verification

- [ ] DevContainer builds successfully and can be opened in VS Code
- [ ] Gradle build completes successfully with `./gradlew build`
- [ ] README.md is comprehensive and includes all necessary sections
- [ ] Directory structure follows Java/Gradle conventions
- [ ] .gitignore includes appropriate patterns for Java/Gradle/IDE files
- [ ] Git repository is in a clean state with all appropriate files tracked
- [ ] Integration test source set is properly configured in Gradle
- [ ] Can run `./gradlew test` successfully (even with no tests yet)
- [ ] Java 21 is properly configured as the project's Java version
- [ ] DevContainer includes necessary development tools and VS Code extensions
