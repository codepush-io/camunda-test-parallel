# Contributing to Camunda Parallel Test Support

Thank you for your interest in contributing to Camunda Parallel Test Support! We welcome contributions from the community.

## Getting Started

### Using DevContainers (Recommended)

1. Install [Docker](https://www.docker.com/products/docker-desktop) and [VS Code](https://code.visualstudio.com/)
2. Install the [Dev Containers extension](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers)
3. Clone this repository
4. Open the repository in VS Code
5. When prompted, click "Reopen in Container" (or use the command palette: "Dev Containers: Reopen in Container")
6. Wait for the container to build and initialize

The DevContainer is pre-configured with Java 21, Gradle, and all necessary development tools.

### Local Development

If you prefer to develop locally without DevContainers:

1. Install [Java 21](https://adoptium.net/) or later
2. Clone this repository
3. Run `./gradlew build` to verify your setup

## Development Workflow

### Building the Project

```bash
./gradlew build
```

### Running Tests

Run unit tests:
```bash
./gradlew test
```

Run integration tests:
```bash
./gradlew integrationTest
```

Run all tests:
```bash
./gradlew check
```

### Code Style

This project uses EditorConfig for consistent code formatting. Most modern IDEs support EditorConfig automatically. The key conventions are:

- **Java files**: 4 spaces for indentation
- **Line length**: Maximum 120 characters
- **Encoding**: UTF-8
- **Line endings**: LF (Unix-style)

Please ensure your IDE is configured to:
- Organize imports automatically
- Format code on save (respecting EditorConfig settings)
- Remove trailing whitespace

## Submitting Changes

### Pull Request Process

1. **Fork the repository** and create a new branch from `main`
2. **Make your changes** following our code style guidelines
3. **Write tests** for your changes
4. **Run all tests** to ensure nothing is broken: `./gradlew check`
5. **Commit your changes** with clear, descriptive commit messages
6. **Push to your fork** and submit a pull request

### Pull Request Guidelines

- Provide a clear description of the problem and solution
- Include relevant issue numbers if applicable
- Ensure all tests pass
- Update documentation as needed
- Keep pull requests focused on a single concern

### Commit Message Guidelines

Follow conventional commit format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

Types:
- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

Example:
```
feat(parallel): add database isolation support

Implement connection pool isolation to allow parallel test execution
with separate database connections per test.

Closes #123
```

## Code Review Process

- All pull requests require review before merging
- Maintainers will review your code for:
  - Correctness and functionality
  - Test coverage
  - Code quality and style
  - Documentation completeness

## Reporting Issues

When reporting issues, please include:

- A clear, descriptive title
- Steps to reproduce the problem
- Expected behavior vs. actual behavior
- Your environment (Java version, OS, etc.)
- Relevant logs or error messages

## Questions?

If you have questions about contributing, feel free to:

- Open an issue with the `question` label
- Reach out to the maintainers

## License

By contributing to this project, you agree that your contributions will be licensed under the Apache License 2.0.
